package com.wuling.opengltrainingcamp.gl;

import android.opengl.GLES10;
import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;

/**
 * @Author: huang xiao xian
 * @Date: 2019/11/28
 * @Des:
 */
public class GLConstant {
    public static final int NO_PROGRAM = 0;
    public static final int NO_UNIFORM_LOC = -1;
    public static final int NO_ATTRIB_LOC = -1;
    public static final int NO_TEXTURE_UNIT = -1;
    public static final int NO_TEX_ID = 0;
    public static final int NO_FRAME_BUFFER = -1;
    public static final int DEF_FRAME_BUFFER = 0;

    public static String errorMsg(int error) {
        switch (error) {
            case EGL10.EGL_SUCCESS:
                return "EGL_SUCCESS";
            case EGL10.EGL_NOT_INITIALIZED:
                return "EGL_NOT_INITIALIZED";
            case EGL10.EGL_BAD_ACCESS:
                return "EGL_BAD_ACCESS";
            case EGL10.EGL_BAD_ALLOC:
                return "EGL_BAD_ALLOC";
            case EGL10.EGL_BAD_ATTRIBUTE:
                return "EGL_BAD_ATTRIBUTE";
            case EGL10.EGL_BAD_CONFIG:
                return "EGL_BAD_CONFIG";
            case EGL10.EGL_BAD_CONTEXT:
                return "EGL_BAD_CONTEXT";
            case EGL10.EGL_BAD_CURRENT_SURFACE:
                return "EGL_BAD_CURRENT_SURFACE";
            case EGL10.EGL_BAD_DISPLAY:
                return "EGL_BAD_DISPLAY";
            case EGL10.EGL_BAD_MATCH:
                return "EGL_BAD_MATCH";
            case EGL10.EGL_BAD_NATIVE_PIXMAP:
                return "EGL_BAD_NATIVE_PIXMAP";
            case EGL10.EGL_BAD_NATIVE_WINDOW:
                return "EGL_BAD_NATIVE_WINDOW";
            case EGL10.EGL_BAD_PARAMETER:
                return "EGL_BAD_PARAMETER";
            case EGL10.EGL_BAD_SURFACE:
                return "EGL_BAD_SURFACE";
            case EGL11.EGL_CONTEXT_LOST:
                return "EGL_CONTEXT_LOST";
            // 以上来自GLUtils.getEGLErrorString
            case GLES20.GL_INVALID_ENUM:
                return "GL_INVALID_ENUM";
            case GLES20.GL_INVALID_VALUE:
                return "GL_INVALID_VALUE";
            case GLES20.GL_INVALID_OPERATION:
                return "GL_INVALID_OPERATION";
            case GLES10.GL_STACK_OVERFLOW:
                return "GL_STACK_OVERFLOW";
            case GLES10.GL_STACK_UNDERFLOW:
                return "GL_STACK_UNDERFLOW";
            case GLES10.GL_OUT_OF_MEMORY:
                return "GL_OUT_OF_MEMORY";
            case GLES20.GL_INVALID_FRAMEBUFFER_OPERATION:
                return "GL_INVALID_FRAMEBUFFER_OPERATION";
            default:
                return "0x" + Integer.toHexString(error);
        }
    }
}
