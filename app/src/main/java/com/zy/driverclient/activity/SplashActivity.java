package com.zy.driverclient.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.zy.driverclient.R;

import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.android.api.InstrumentedActivity;


public class SplashActivity extends InstrumentedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //建立线程使图片静止2S;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            private Intent intent;

            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
                boolean isFirst = sp.getBoolean("isFirst", true);
                //判断是否第一次进入，是进入引导页，不是进入主页;
                intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        };
        timer.schedule(task, 2000);
    }

}
