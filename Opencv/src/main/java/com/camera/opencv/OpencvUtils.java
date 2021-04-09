package com.camera.opencv;

public class OpencvUtils {

    static {
        System.loadLibrary("fly-opencv-native");
    }


    public static native String getOpencvVersion();
}
