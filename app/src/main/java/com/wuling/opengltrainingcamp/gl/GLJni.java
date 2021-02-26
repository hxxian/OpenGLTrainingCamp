package com.wuling.opengltrainingcamp.gl;

import android.graphics.Bitmap;

/**
 * @Author: huang xiao xian
 * @Date: 2021/2/23
 * @Des:
 */
public class GLJni {

    static {
        System.loadLibrary("native-lib");
    }

    public static native void glReadPixels(
            int x,
            int y,
            int width,
            int height,
            int format,
            int type
    );

    public static native void glTexSubImage2D(
            int target,
            int level,
            int xoffset,
            int yoffset,
            int width,
            int height,
            int format,
            int type
    );

    public static native void  glMapBufferToPBO(
            Bitmap bitmap,
            int width,
            int height,
            int dataSize
    );
}