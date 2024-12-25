#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_me_zyrouge_symphony_metaphony_Metaphony_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}