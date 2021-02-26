#include <jni.h>
#include <string>
#include <GLES2/gl2.h>
#include <GLES3/gl3.h>
#include <android/bitmap.h>
#include <android/log.h>

#define LOG_TAG "GLJNI"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG, __VA_ARGS__)


extern "C"
JNIEXPORT void JNICALL
Java_com_wuling_opengltrainingcamp_gl_GLJni_glReadPixels(
        JNIEnv *env, jclass cls, jint x, jint y, jint width, jint height,
        jint format, jint type) {
    glReadPixels(x, y, width, height, format, type, 0);
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_wuling_opengltrainingcamp_gl_GLJni_glTexSubImage2D(JNIEnv *env, jclass clazz, jint target,
                                                            jint level, jint xoffset, jint yoffset,
                                                            jint width, jint height, jint format,
                                                            jint type) {
    glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, target, 0);
    // TODO: implement glTexSubImage2D()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_wuling_opengltrainingcamp_gl_GLJni_glMapBufferToPBO(JNIEnv *env, jclass clazz,
                                                             jobject bitmap, jint width,
                                                             jint height, jint data_size) {
    AndroidBitmapInfo bmpInfo;
    void *bmpPixels;

    if (AndroidBitmap_getInfo(env, bitmap, &bmpInfo) < 0) {
        // TODO log
        return;
    }


    int dataSize = bmpInfo.width * bmpInfo.height * 4;

    LOGW("dataSize %d" , dataSize);

    GLubyte *buffer = (GLubyte *) (glMapBufferRange(GL_PIXEL_UNPACK_BUFFER, 0, dataSize,
                                                    GL_MAP_WRITE_BIT |
                                                    GL_MAP_INVALIDATE_BUFFER_BIT));

    if (buffer) {
        LOGI("进入内存拷贝");
        AndroidBitmap_lockPixels(env, bitmap, &bmpPixels);
        memcpy(buffer, bmpPixels, static_cast<size_t>(dataSize));
        LOGI("拷贝完成");
//        int randomRow = rand() % (height - 5);
//        memset(buffer + randomRow * width * 4, 188,
//               static_cast<size_t>(width * 4 * 5));
        AndroidBitmap_unlockPixels(env, bitmap);
        glUnmapBuffer(GL_PIXEL_UNPACK_BUFFER);
    }
}
