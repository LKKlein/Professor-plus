#include <jni.h>
#include <string>

extern "C"
jstring
Java_cn_edu_swufe_fife_professor_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
