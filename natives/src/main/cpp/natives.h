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
 * Under section 7 of the GNU General Public License v3, the following additional
 * terms apply to this program:
 *
 *  (b) You must preserve reasonable legal notices and author attributions in
 *      the program.
 *  (c) You must not misrepresent the origin of this program, and need to mark
 *      modified versions in reasonable ways as different from the original
 *      version (such as changing the name and logos).
 *  (d) You may not use the names of licensors or authors for publicity
 *      purposes, without explicit written permission.
 */

#ifndef ANDPROX_NATIVES_H
#define ANDPROX_NATIVES_H

#include <string.h>
#include "jnihelper.h"
#ifdef ANDROID
#include <android/log.h>
#endif
#include "util.h"

#ifdef ANDROID
// Android log function wrappers
static const char* kTAG = "Natives";
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))
#define LOGW(...) \
  ((void)__android_log_print(ANDROID_LOG_WARN, kTAG, __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, kTAG, __VA_ARGS__))

#else
// Non-Android JNI
#define LOGI PrintAndLog
#define LOGW PrintAndLog
#define LOGE PrintAndLog

#endif

JNIEXPORT void JNICALL
Java_au_id_micolous_andprox_natives_Natives_initProxmark(JNIEnv *env, jclass type);

// Wrappers for uart_android that seal g_ctx
void nsw_close(JNIEnv* env, jobject nativeSerialWrapper);
jint nsw_receive(JNIEnv* env, jobject nativeSerialWrapper, jbyteArray recvBuffer);
jboolean nsw_send(JNIEnv* env, jobject nativeSerialWrapper, jbyteArray sendBuffer);
void natives_printandlog(JNIEnv* env, jstring s);
const char* natives_getexecutabledirectory();

#endif //ANDPROX_NATIVES_H
