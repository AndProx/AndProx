/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
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

#include <uart.h>
#include <comms.h>
#include <ui.h>
#include <cmdmain.h>
#include <unistd.h>
#include <errno.h>
#include <usb_cmd.h>
#include "natives.h"
#include "uart_android.h"
#include "fakemain.h"

#if !defined(PM3_GIT_VER)
#error "Expected PM3_GIT_VER to be defined"
#endif

#if !defined(PM3_TS)
#error "Expected PM3_TS to be defined"
#endif

#define SERIAL_PORT_LABEL "AndProx virtual serial port"

UsbCommand versionResp = {0, {0, 0, 0}};

bool OpenProxmarkAndroid(JNIEnv* env, JavaVM* vm, jobject nsw) {
    uart_open_android(env, vm, nsw);
    return OpenProxmark(SERIAL_PORT_LABEL, /* waitCOMPort */ false, /* timeout */ 0, /* flash_mode */ false);
}

JNIEXPORT void JNICALL
Java_au_id_micolous_andprox_natives_Natives_initProxmark(JNIEnv *env, jclass type) {
    LOGI("Calling initProxmark");

    // Initialise all the things we want.
    PlotGridXdefault = 64;
    PlotGridYdefault = 64;
    showDemod = true;
    CursorScaleFactor = 1;

    SetLogFilename(NULL);
    SetOffline(true);
    // TODO: reset more stuff here
}

JNIEXPORT void JNICALL
Java_au_id_micolous_andprox_natives_Natives_startReaderThread(JNIEnv *env, jclass type, jobject nsw) {
    LOGI("starting reader thread");
    SetOffline(false);
    memset((void*)(&versionResp), 0, sizeof(versionResp));
    OpenProxmarkAndroid(env, g_ctx.javaVM, nsw);
}

JNIEXPORT void JNICALL
Java_au_id_micolous_andprox_natives_Natives_stopReaderThread(JNIEnv *env, jclass type) {
    LOGI("asking reader thread to stop");
    CloseProxmark();
    SetOffline(true);
}


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    memset(&g_ctx, 0, sizeof(g_ctx));

    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR; // JNI version not supported.
    }

    g_ctx.javaVM = vm;

    // Lookup function table for NativeSerialWrapper
    jclass clz = (*env)->FindClass(env, "au/id/micolous/andprox/natives/NativeSerialWrapper");
    g_ctx.jcNativeSerialWrapper = (*env)->NewGlobalRef(env, clz);

    g_ctx.jmNSWClose = (*env)->GetMethodID(env, g_ctx.jcNativeSerialWrapper, "close", "()V");

    if (!g_ctx.jmNSWClose) {
        LOGE("Failed to retrieve NativeSerialWrapper.close() methodID @ line %d", __LINE__);
        return JNI_ERR;
    }

    g_ctx.jmNSWReceive = (*env)->GetMethodID(env, g_ctx.jcNativeSerialWrapper, "receive", "([B)I");

    if (!g_ctx.jmNSWReceive) {
        LOGE("Failed to retrieve NativeSerialWrapper.receive() methodID @ line %d", __LINE__);
        return JNI_ERR;
    }

    g_ctx.jmNSWSend = (*env)->GetMethodID(env, g_ctx.jcNativeSerialWrapper, "send", "([B)Z");

    if (!g_ctx.jmNSWSend) {
        LOGE("Failed to retrieve NativeSerialWrapper.send() methodID @ line %d", __LINE__);
        return JNI_ERR;
    }

    clz = (*env)->FindClass(env, "au/id/micolous/andprox/natives/Natives");
    g_ctx.jcNatives = (*env)->NewGlobalRef(env, clz);

    g_ctx.jmPrintAndLog = (*env)->GetStaticMethodID(env, g_ctx.jcNatives, "javaPrintAndLog", "(Ljava/lang/String;)V");
    if (!g_ctx.jmPrintAndLog) {
        LOGE("Failed to retrieve Natives.javaPrintAndLog() methodID @ line %d", __LINE__);
        return JNI_ERR;
    }

    g_ctx.jmPrintf = (*env)->GetStaticMethodID(env, g_ctx.jcNatives, "javaPrintf", "(Ljava/lang/String;)V");
    if (!g_ctx.jmPrintf) {
        LOGE("Failed to retrieve Natives.javaPrintf() methodID @ line %d", __LINE__);
        return JNI_ERR;
    }

    jmethodID jmPM3StorageRoot = (*env)->GetStaticMethodID(env, g_ctx.jcNatives, "getPM3StorageRoot", "()Ljava/lang/String;");

    if (!jmPM3StorageRoot) {
        LOGE("Failed to retrieve Natives.getPM3StorageRoot methodID @ line %d", __LINE__);
        return JNI_ERR;
    }

    jstring executableDirectory = (*env)->CallStaticObjectMethod(env, g_ctx.jcNatives, jmPM3StorageRoot);

    g_ctx.executable_directory = (char *) (*env)->GetStringUTFChars(env, executableDirectory, NULL);
    if (!g_ctx.executable_directory) {
        LOGE("Unable to get executable directory @ line %d", __LINE__);
        return JNI_ERR;
    }

    (*env)->DeleteLocalRef(env, executableDirectory);
    LOGI("Executable directory = %s", g_ctx.executable_directory);

    LOGI("Completed OnLoad");

    return JNI_VERSION_1_6;
}

JNIEXPORT jstring JNICALL
Java_au_id_micolous_andprox_natives_Natives_sendCmdVersion(JNIEnv *env, jclass type) {
// CmdVersion keeps its own cache, so we should reimplement here a little...
    jstring s = NULL;

    clearCommandBuffer();
    UsbCommand c = {CMD_VERSION};

    if (versionResp.arg[0] == 0 && versionResp.arg[1] == 0) { // no cached information available
        SendCommand(&c);
        if (WaitForResponseTimeout(CMD_ACK, &versionResp, 10001)) {
            PrintAndLog("Prox/RFID mark3 RFID instrument");
            PrintAndLog((char*)versionResp.d.asBytes);

            s = (*env)->NewStringUTF(env, (char*)versionResp.d.asBytes);
            // TODO: Implement lookupChipID
            //lookupChipID(resp.arg[0], resp.arg[1]);
        } else {
            PrintAndLog("got no response");
        }
    } else {
        PrintAndLog("[[[ Cached information ]]]\n");
        PrintAndLog("Prox/RFID mark3 RFID instrument");
        PrintAndLog((char*)versionResp.d.asBytes);
        //lookupChipID(resp.arg[0], resp.arg[1]);
        s = (*env)->NewStringUTF(env, (char*)versionResp.d.asBytes);
    }

    return s;
}

JNIEXPORT void JNICALL
Java_au_id_micolous_andprox_natives_Natives_sendCmd(JNIEnv *env, jclass type, jstring cmd_) {
    char *cmd = (char*)((*env)->GetStringUTFChars(env, cmd_, 0));

    // Many parts of the PM3 client will assume that they can read any write from pwd. So we set
    // pwd to whatever the PM3 "executable directory" is, to get consistent behaviour.
    int ret = chdir(get_my_executable_directory());
    if (ret == -1) {
        LOGW("Couldn't chdir(get_my_executable_directory()), errno=%s", strerror(errno));
    }

    char pwd[1024];
    memset((void*)&pwd, 0, sizeof(pwd));
    getcwd((char*)&pwd, sizeof(pwd));

    LOGI("pwd = %s", pwd);

    ret = CommandReceived(cmd);

    if (ret == 99) {
        // exit / quit
        // TODO: implement this
        PrintAndLog("Asked to exit, can't really do that yet...");
    }

    (*env)->ReleaseStringUTFChars(env, cmd_, cmd);
}


JNIEXPORT jstring JNICALL
Java_au_id_micolous_andprox_natives_Natives_getProxmarkClientVersion(JNIEnv *env, jclass type) {
    return (*env)->NewStringUTF(env, PM3_GIT_VER);
}



JNIEXPORT jstring JNICALL
Java_au_id_micolous_andprox_natives_Natives_getProxmarkClientBuildTimestamp(JNIEnv *env,
                                                                            jclass type) {
    return (*env)->NewStringUTF(env, PM3_TS);
}

JNIEXPORT jobject JNICALL
Java_au_id_micolous_andprox_natives_Natives_sendCmdTune__ZZ(JNIEnv *env, jclass type, jboolean lf,
                                                            jboolean hf) {

    if (IsOffline()) {
        PrintAndLog("Not possible while offline.");
        return NULL;
    }

    int timeout = 0;
    uint64_t arg = 0;
    if (lf) {
        arg |= FLAG_TUNE_LF;
    }
    if (hf) {
        arg |= FLAG_TUNE_HF;
    }

    UsbCommand c = {CMD_MEASURE_ANTENNA_TUNING, {arg, 0, 0}};
    SendCommand(&c);

    UsbCommand resp;
    while (!WaitForResponseTimeout(CMD_MEASURED_ANTENNA_TUNING, &resp, 1000)) {
        timeout++;
        if (timeout > 7) {
            PrintAndLog("No response from Proxmark. Aborting...");
            return NULL;
        }
    }

    int graphSize = 256;
    jint graphData[graphSize];
    jintArray graphDataArray = (*env)->NewIntArray(env, graphSize);
    if (graphDataArray == NULL) {
        // OOM
        PrintAndLog("Unable to allocate graphdata buffer");
        return NULL;
    }

    for (int i=0; i<graphSize; i++) {
        graphData[i] = ((jint)(resp.d.asBytes[i]) & 0xFF) << 9;
        // Upstream has "- 128" here, but this isn't actually correct.
    }

    (*env)->SetIntArrayRegion(env, graphDataArray, 0, graphSize, graphData);

    jclass cls = (*env)->FindClass(env, "au/id/micolous/andprox/natives/TuneResult");
    jmethodID meth = (*env)->GetMethodID(env, cls, "<init>", "(JJJ[I)V");
    jobject a = (*env)->NewObject(env, cls, meth, resp.arg[0], resp.arg[1], resp.arg[2], graphDataArray);
    (*env)->DeleteLocalRef(env, graphDataArray);
    return a;
}

JNIEXPORT jboolean JNICALL
Java_au_id_micolous_andprox_natives_Natives_isOffline(JNIEnv *env, jclass type) {
    return (jboolean) IsOffline();
}
