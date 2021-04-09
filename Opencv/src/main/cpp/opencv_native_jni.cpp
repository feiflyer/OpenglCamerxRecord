//
// Created by bbyy-liangchuanfei on 4/9/21.
//

#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_camera_opencv_OpencvUtils_getOpencvVersion(JNIEnv *env, jclass clazz) {
    std::string hello = CV_VERSION;
    return env->NewStringUTF(hello.c_str());
}
