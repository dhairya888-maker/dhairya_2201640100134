#include <jni.h>
#include <android/log.h>
#include <mutex>

#include "opencv_processor.hpp"
#include "gl_texture_uploader.hpp"

#define LOG_TAG "EdgePipeline"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {
std::mutex gMutex;
OpenCvProcessor gProcessor;
GlTextureUploader gUploader;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgeviewer_NativePipeline_nativeInit(
        JNIEnv* env,
        jobject /*thiz*/,
        jint width,
        jint height) {
    std::lock_guard<std::mutex> lock(gMutex);
    gProcessor.initialise(width, height);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_edgeviewer_NativePipeline_nativeProcessFrame(
        JNIEnv* env,
        jobject /*thiz*/,
        jobject yBuffer,
        jobject uBuffer,
        jobject vBuffer,
        jint pixelStride,
        jint rowStride,
        jint width,
        jint height,
        jboolean edgeMode) {
    std::lock_guard<std::mutex> lock(gMutex);
    auto* yPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(yBuffer));
    auto* uPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(uBuffer));
    auto* vPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(vBuffer));

    if (!yPtr || !uPtr || !vPtr) {
        LOGE("Direct buffer address unavailable");
        return 0;
    }

    return gProcessor.processFrame(
        yPtr,
        uPtr,
        vPtr,
        pixelStride,
        rowStride,
        width,
        height,
        edgeMode
    );
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgeviewer_NativePipeline_nativeUploadToTexture(
        JNIEnv* env,
        jobject /*thiz*/,
        jint textureId) {
    std::lock_guard<std::mutex> lock(gMutex);
    gUploader.bindTexture(static_cast<GLuint>(textureId));
    gUploader.upload(gProcessor.rgba());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgeviewer_NativePipeline_nativeRelease(
        JNIEnv* env,
        jobject /*thiz*/) {
    std::lock_guard<std::mutex> lock(gMutex);
}

