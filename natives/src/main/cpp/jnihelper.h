//
// Convenience wrappers for JNI.
//

#ifndef ANDPROX_JNI_H
#define ANDPROX_JNI_H
#include <jni.h>

jint InitGlobalJniVariables(JavaVM* jvm, JNIEnv** jni);
JNIEnv* AttachCurrentThreadIfNeeded();

#endif //ANDPROX_JNI_H
