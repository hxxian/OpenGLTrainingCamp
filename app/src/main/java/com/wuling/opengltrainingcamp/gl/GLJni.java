package com.wuling.opengltrainingcamp.gl;

/**
 * @Author: huang xiao xian
 * @Date: 2021/2/23
 * @Des:
 */
public class GLJni {

//    static {
//        System.loadLibrary("native-lib");
//    }

    public static native void glReadPixels(
            int x,
            int y,
            int width,
            int height,
            int format,
            int type
    );

}