package com.wuling.opengltrainingcamp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wuling.opengltrainingcamp.common.GlobalContext;
import com.wuling.opengltrainingcamp.demo1.DemoActivity1;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private TextView tvvBO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalContext.context = getApplicationContext();

        tvvBO = findViewById(R.id.tv_vbo);
        tvvBO.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_vbo:
                startActivity(new Intent(this, DemoActivity1.class));
                break;
        }
    }

}