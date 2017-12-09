/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2017 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Under section 7 of the GNU General Public License v3, the following "further
 * restrictions" apply to this program:
 *
 *  (b) You must preserve reasonable legal notices and author attributions in
 *      the program.
 *  (c) You must not misrepresent the origin of this program, and need to mark
 *      modified versions in reasonable ways as different from the original
 *      version (such as changing the name and logos).
 *  (d) You may not use the names of licensors or authors for publicity
 *      purposes, without explicit written permission.
 */

#include "fakemain.h"
#include "natives.h"
#include <jni.h>
#include <stdio.h>
#include <malloc.h>

pthread_t reader_thread;

const char* get_my_executable_path(void)
{
    // Never used...
    return "";
}

const char* get_my_executable_directory(void)
{
    // This gets called by cmdhfmfhard.c and cmdscript.c to find where scripts and tables are
    return g_ctx.executable_directory;
}

void PrintAndLog(char *fmt, ...)
{
    char buf[1024];
    GET_ENV(g_ctx.javaVM)
    if (env == NULL) return;

    // Write out the message, formatted, into buf
    va_list argptr;
    va_start(argptr, fmt);
    vsnprintf(buf, sizeof(buf), fmt, argptr);
    va_end(argptr);

    // Send the string to Java
    jstring s = (*env)->NewStringUTF(env, buf);
    (*env)->CallStaticVoidMethod(env, g_ctx.jcNatives, g_ctx.jmPrintAndLog, s);
    (*env)->DeleteLocalRef(env, s);
}

void PrintAndLogL(char* s, size_t len) {
    GET_ENV(g_ctx.javaVM)

    char* buf = malloc(sizeof(len) + 1);
    memset((void*)buf, 0, sizeof(len) + 1);
    memcpy(buf, s, len);

    // Send the string to Java
    jstring js = (*env)->NewStringUTF(env, buf);
    (*env)->CallStaticVoidMethod(env, g_ctx.jcNatives, g_ctx.jmPrintAndLog, js);
    (*env)->DeleteLocalRef(env, js);
    free(buf);
}