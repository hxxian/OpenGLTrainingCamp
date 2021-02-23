#include <jni.h>
#include <string>
#include <GLES2/gl2.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_wuling_opengltrainingcamp_gl_GLJni_glReadPixels(
        JNIEnv *env, jclass cls, jint x, jint y, jint width, jint height,
        jint format, jint type) {
    glReadPixels(x, y, width, height, format, type, 0);
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
}

