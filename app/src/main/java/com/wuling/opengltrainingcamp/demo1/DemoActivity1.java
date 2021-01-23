package com.wuling.opengltrainingcamp.demo1;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.wuling.opengltrainingcamp.R;

public class DemoActivity1 extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private SimpleRender render;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo1);

        glSurfaceView = findViewById(R.id.demo1_surfaceview);
        glSurfaceView.setEGLContextClientVersion(3);
        render = new SimpleRender();
        glSurfaceView.setRenderer(render);
        /*渲染方式，RENDERMODE_WHEN_DIRTY表示被动渲染，只有在调用requestRender或者onResume等方法时才会进行渲染。RENDERMODE_CONTINUOUSLY表示持续渲染*/
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        render.destroy();
        glSurfaceView.requestRender();
        glSurfaceView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}