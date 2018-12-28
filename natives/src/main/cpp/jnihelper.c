/*
 * jnihelper.c
 *
 * Adapted from WebRTC project:
 * https://webrtc.googlesource.com/src/+/master/sdk/android/src/jni/jvm.cc
 *
 * Copyright 2018 The WebRTC project authors. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Google nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <asm/unistd.h>
#include <pthread.h>
#include <sys/prctl.h>
#include <sys/syscall.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include "jnihelper.h"

static JavaVM* g_jvm = NULL;
static pthread_once_t g_jni_ptr_once = PTHREAD_ONCE_INIT;

// Key for per-thread JNIEnv* data.  Non-NULL in threads attached to |g_jvm| by
// AttachCurrentThreadIfNeeded(), NULL in unattached threads and threads that
// were attached by the JVM because of a Java->native call.
static pthread_key_t g_jni_ptr;

// Return a |JNIEnv*| usable on this thread or NULL if this thread is detached.
JNIEnv* GetEnv() {
    void* env = NULL;
    if (g_jvm == NULL) {
        // Error
        return NULL;
    }
    jint status = (*g_jvm)->GetEnv(g_jvm, &env, JNI_VERSION_1_6);
    if (!(((env != NULL) && (status == JNI_OK)) ||
              ((env == NULL) && (status == JNI_EDETACHED)))) {
        // Error
        return NULL;
    }

    return (JNIEnv*)env;
}

static void ThreadDestructor(void* prev_jni_ptr) {
    // This function only runs on threads where |g_jni_ptr| is non-NULL, meaning
    // we were responsible for originally attaching the thread, so are responsible
    // for detaching it now.  However, because some JVM implementations (notably
    // Oracle's http://goo.gl/eHApYT) also use the pthread_key_create mechanism,
    // the JVMs accounting info for this thread may already be wiped out by the
    // time this is called. Thus it may appear we are already detached even though
    // it was our responsibility to detach!  Oh well.
    if (!GetEnv())
        return;
    if (GetEnv() == prev_jni_ptr) {
        //<< "Detaching from another thread: " << prev_jni_ptr << ":" << GetEnv();
        return;
    }

    jint status = (*g_jvm)->DetachCurrentThread(g_jvm);
    if (status == JNI_OK) {
        // << "Failed to detach thread: " << status;
        return;
    }
    if (!GetEnv()) {
        //  << "Detaching was a successful no-op???";
        return;
    }
}

static void CreateJNIPtrKey() {
    pthread_key_create(&g_jni_ptr, &ThreadDestructor);
}

jint InitGlobalJniVariables(JavaVM* jvm, JNIEnv** jni) {
    if (g_jvm != NULL) {
        // g_jvm already set
        // << "InitGlobalJniVariables!";
        return -1;
    }
    g_jvm = jvm;
    if (g_jvm == NULL) {
        // "InitGlobalJniVariables handed NULL?";
        return -1;
    }

    int err = pthread_once(&g_jni_ptr_once, &CreateJNIPtrKey);
    if (err != 0) {
        // "pthread_once";
        return -1;
    }

    *jni = NULL;
    if ((*jvm)->GetEnv(jvm, (void**)jni, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    return JNI_VERSION_1_6;
}

// Return a |JNIEnv*| usable on this thread.  Attaches to |g_jvm| if necessary.
// Returns NULL on error
JNIEnv* AttachCurrentThreadIfNeeded() {
    JNIEnv* jni = GetEnv();
    if (jni != NULL) {
        return jni;
    }
    if (pthread_getspecific(g_jni_ptr) != NULL) {
        // "TLS has a JNIEnv* but not attached?";
        return NULL;
    }

// Deal with difference in signatures between Oracle's jni.h and Android's.
#ifdef _JAVASOFT_JNI_H_  // Oracle's jni.h violates the JNI spec!
    void* env = NULL;
#else
    JNIEnv* env = NULL;
#endif
    if ((*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL) != JNI_OK) {
        // "Failed to attach thread";
        return NULL;
    }
    if (env == NULL) {
        // "AttachCurrentThread handed back NULL!"
        return NULL;
    }

#ifdef _JAVASOFT_JNI_H_  // Oracle's jni.h violates the JNI spec!
    jni = (JNIEnv*)env;
#else
    jni = env;
#endif

    if (pthread_setspecific(g_jni_ptr, jni) != 0) {
        // << "pthread_setspecific";
        return NULL;
    }
    return jni;
}
