package com.wuling.opengltrainingcamp.demo1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.wifi.WifiManager;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.wuling.opengltrainingcamp.common.GlobalContext;
import com.wuling.opengltrainingcamp.executor.ExecutorSupplier;
import com.wuling.opengltrainingcamp.gl.GLConstant;
import com.wuling.opengltrainingcamp.gl.GLJni;
import com.wuling.opengltrainingcamp.gl.GLUtil;
import com.wuling.opengltrainingcamp.gl.TextureUtil;
import com.wuling.opengltrainingcamp.util.FileUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Author: huang xiao xian
 * @Date: 2021/1/18
 * @Des:
 */
public class SimpleRender implements GLSurfaceView.Renderer {

    private static final String TAG = "SimpleRender";

    public static final String VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "   textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";

    public static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    private int programId;
    private int a_position;
    private int a_inputTextureCoordinate;
    private int u_inputImageTexture;

    private Bitmap bitmap;
    private int textureId;
    private boolean release;
    private int bmpWidth;
    private int bmpHeight;
    private int surfaceWidth;
    private int surfaceHeight;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private FloatBuffer vertexBuffer2;

    private boolean initialized;


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.i(TAG, "onSurfaceCreated");
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Log.i(TAG, "onSurfaceChanged");

        surfaceWidth = width;
        surfaceHeight = height;
        config();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        Log.i(TAG, "onDrawFrame");
        if (release) {
            release();
        } else {
            draw();
            draw();
            draw();
        }
    }

    private void config() {
        if (initialized) {
            return;
        }

        programId = GLUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        a_position = GLES20.glGetAttribLocation(programId, "position");
        a_inputTextureCoordinate = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");
        u_inputImageTexture = GLES20.glGetUniformLocation(programId, "inputImageTexture");

        bitmap = FileUtil.getBitmapFromAsset(GlobalContext.context, "picture/5520_3680.jpg");
        bmpWidth = bitmap.getWidth();
        bmpHeight = bitmap.getHeight();

        textureId = GLUtil.loadTexture(bitmap);

        vertexBuffer = ByteBuffer
                .allocateDirect(TextureUtil.VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(getVertexPos()).position(0);

        vertexBuffer2 = ByteBuffer
                .allocateDirect(TextureUtil.VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer2.put(TextureUtil.VERTEX).position(0);

        textureBuffer = ByteBuffer
                .allocateDirect(TextureUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureBuffer.put(TextureUtil.TEXTURE_NO_ROTATION).position(0);

        createFBO();

        initPBOs();

        initialized = true;
    }

    public int loadTexture() {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        return textureHandle[0];
    }

    // 计算顶点坐标，使得纹理的绘制效果是：fitCenter
    private float[] getVertexPos() {
        int width = bmpWidth;
        int height = bmpHeight;

        float bmpRatio = width / (float) height;
        float surfaceViewRatio = surfaceWidth / (float) surfaceHeight;
        RectF rectF = new RectF();
        float surfaceCenterX = 0;
        float surfaceCenterY = 0;
        rectF.set(0, 0, surfaceWidth, surfaceHeight);
        surfaceCenterX = rectF.centerX();
        surfaceCenterY = rectF.centerY();

        RectF bmpRectF = new RectF();

        if (bmpRatio > surfaceViewRatio) {
            width = surfaceWidth;
            height = (int) (width / bmpRatio);
            bmpRectF.set(0, 0, width, height);

            float halfBmpH = bmpHeight / 2.0f;
            rectF.top = surfaceCenterY + halfBmpH;
            rectF.bottom = surfaceCenterY - halfBmpH;
        } else {
            height = surfaceHeight;
            width = (int) (height * bmpRatio);
            bmpRectF.set(0, 0, width, height);

            float halfBmpW = bmpWidth / 2.0f;
            rectF.left = surfaceCenterX - halfBmpW;
            rectF.right = surfaceCenterX + halfBmpW;
        }

        // 将图片显示在surfaceView中间
        float centerOffsetX = surfaceCenterX - bmpRectF.centerX();
        float centerOffsetY = surfaceCenterY - bmpRectF.centerY();
        bmpRectF.left += centerOffsetX;
        bmpRectF.right += centerOffsetX;
        bmpRectF.top += centerOffsetY;
        bmpRectF.bottom += centerOffsetY;

        // 归一化，转化为OpenGL世界坐标
        bmpRectF.left = (bmpRectF.left - surfaceCenterX) / surfaceCenterX;
        bmpRectF.right = (bmpRectF.right - surfaceCenterX) / surfaceCenterX;
        bmpRectF.top = (bmpRectF.top - surfaceCenterY) / surfaceCenterY;
        bmpRectF.bottom = (bmpRectF.bottom - surfaceCenterY) / surfaceCenterY;

        float[] pos = {
                bmpRectF.left, bmpRectF.bottom,
                bmpRectF.right, bmpRectF.bottom,
                bmpRectF.left, bmpRectF.top,
                bmpRectF.right, bmpRectF.top,
        };

        return pos;
    }

    private void draw() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        GLES20.glUseProgram(programId);

        GLES20.glViewport(0, 0, bmpWidth, bmpHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0, 0, 0, 0);

        onDraw(textureId, vertexBuffer2, textureBuffer);

//        Bitmap bitmap = readPixelsFromPBO();
//        if (bitmap != null) {
//            bitmap.recycle();
//        }

        pboToTextureObject();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);


        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0, 0, 0, 0);
        onDraw(fboTextureId, vertexBuffer, textureBuffer);

    }

    private void onDraw(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glEnableVertexAttribArray(a_inputTextureCoordinate);

        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(a_inputTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

//        if (vboId < 0) {
//            vboId = createVertexBuffer();
//        }

//        useVboDraw();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(u_inputImageTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_inputTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE);

    }

    public void destroy() {
        this.release = true;
    }

    //每一次取点的时候取几个点
    private int vboId = -1;
    private final int COORDS_PER_VERTEX = 2;
    private float vertexData[] = TextureUtil.VERTEX;
    private final int vertexCount = vertexData.length / COORDS_PER_VERTEX;
    //每一次取的总的点 大小
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public int createVertexBuffer() {
        int[] vbos = new int[1];
        // 1、创建VBP
        GLES20.glGenBuffers(vbos.length, vbos, 0);
        int vboId = vbos[0];
        //2、绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        // 3、分配VBO缓存大小
        int bufferSize = TextureUtil.VERTEX.length * 4 + TextureUtil.TEXTURE_NO_ROTATION.length * 4;
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferSize, null, GLES20.GL_STREAM_DRAW);
        // 4、给VBO设置顶点数据
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, TextureUtil.VERTEX.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, TextureUtil.VERTEX.length * 4, TextureUtil.TEXTURE_NO_ROTATION.length * 4, textureBuffer);
        // 5、解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        return vboId;
    }

    private void useVboDraw() {
        //1. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //2. 设置顶点数据
        GLES20.glVertexAttribPointer(a_position, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, 0);
        GLES20.glVertexAttribPointer(a_inputTextureCoordinate, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexData.length * 4);
        //3. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }


    private int fboId;
    private int fboTextureId;

    private void createFBO() {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is  null");
        }

        //1. 创建FBO
        int[] fbos = new int[1];
        GLES20.glGenFramebuffers(1, fbos, 0);
        fboId = fbos[0];
        //2. 绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        //3. 创建FBO纹理对象
        fboTextureId = createTexture();

        //4. 把纹理绑定到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fboTextureId, 0);

        //5. 设置FBO分配内存大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(),
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        //6. 检测是否绑定从成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
                != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("zzz", "glFramebufferTexture2D error");
        }
        //7. 解绑纹理和FBO
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private int createTexture() {
        int[] textureIds = new int[1];
        //创建纹理
        GLES20.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            return 0;
        }
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        return textureIds[0];
    }

    private int[] pbos = new int[2];
    private int[] pbos2 = new int[2];
    private int index = 0;
    private int nextIndex = 1;
    private int size;

    private void initPBOs() {
        size = bmpWidth * bmpHeight * 4;
        GLES30.glGenBuffers(2, pbos, 0);
        //绑定到第一个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[0]);
        //设置内存大小
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_STATIC_READ);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[1]);
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_STATIC_READ);

        //解除绑定PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0);


        GLES30.glGenBuffers(2, pbos2, 0);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pbos2[0]);
        GLES30.glBufferData(GLES30.GL_PIXEL_UNPACK_BUFFER, size, null, GLES30.GL_STREAM_DRAW);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pbos2[1]);
        GLES30.glBufferData(GLES30.GL_PIXEL_UNPACK_BUFFER, size, null, GLES30.GL_STREAM_DRAW);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, 0);
    }

    private Bitmap readPixelsFromPBO() {

//        byte[] bitmapBuffer = new byte[bmpWidth * bmpHeight];
//        ByteBuffer buffer = ByteBuffer.wrap(bitmapBuffer);
//        buffer.position(0);
//        try {
//            long s = System.currentTimeMillis();
//            GLES20.glReadPixels(0, 0, bmpWidth, bmpHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, buffer);
//            long e = System.currentTimeMillis();
//            Log.w(TAG, "普通方式读取耗时：" + (e - s));
//        } catch (GLException e) {
//            e.printStackTrace();
//        }

        //绑定到第一个PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[index]);
        long start = System.currentTimeMillis();
        GLES30.glReadPixels(0, 0, bmpWidth, bmpHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, 0); // read pixels
        long end = System.currentTimeMillis();
        Log.w(TAG, "glReadPixels耗时: " + (end - start));

        //绑定到第二个PBO
        //glMapBufferRange会等待DMA传输完成，所以需要交替使用pbo
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[nextIndex]);

        //映射内存, glMapBufferRange会等待DMA传输完成，所以需要交替使用pbo
        long start2 = System.currentTimeMillis();
        ByteBuffer byteBuffer = (ByteBuffer) GLES30.glMapBufferRange(GLES30.GL_PIXEL_PACK_BUFFER, 0, 4 * bmpWidth * bmpHeight, GLES30.GL_MAP_READ_BIT);

        long end2 = System.currentTimeMillis();
        Log.w(TAG, "glMapBufferRange 耗时: " + (end2 - start2));

        if (byteBuffer != null && byteBuffer.remaining() > 0) {
            //解除映射
            GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER);
        }
        //解除绑定PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, GLES20.GL_NONE);
        //交换索引
        index = (index + 1) % 2;
        nextIndex = (nextIndex + 1) % 2;

        Bitmap bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(byteBuffer);
        return bitmap;
    }

    private int pboTextureId = 0;

    private void pboToTextureObject() {
        if (pboTextureId == 0) {
            pboTextureId = loadTexture();
        }

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pbos2[index]);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, pboTextureId);

        GLES30.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bmpWidth, bmpHeight, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, null);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pbos2[nextIndex]);

        ByteBuffer byteBuffer = (ByteBuffer) GLES30.glMapBufferRange(GLES30.GL_PIXEL_UNPACK_BUFFER,
                0, size, GLES30.GL_MAP_WRITE_BIT);

        if (byteBuffer.remaining() > 0) {
            byteBuffer.position(0);
            bitmap.copyPixelsToBuffer(byteBuffer);

            GLES30.glUnmapBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER);
        }

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        index = (index + 1) % 2;
        nextIndex = (nextIndex + 1) % 2;

        Bitmap bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        byteBuffer.position(0);
        bitmap.copyPixelsFromBuffer(byteBuffer);
        if (bitmap != null) {
            bitmap.recycle();
        }

//        Bitmap bitmap = GLUtil.readPixelsFrom2DTexture(pboTextureId, 0, 0, bmpWidth, bmpHeight);


    }

    public void release() {
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteTextures(1, new int[]{
                textureId
        }, 0);
        textureId = GLConstant.NO_PROGRAM;
        Optional.ofNullable(bitmap).ifPresent(bmp -> bmp.recycle());
        GLES20.glDeleteBuffers(1, new int[]{vboId}, 0);
    }
}