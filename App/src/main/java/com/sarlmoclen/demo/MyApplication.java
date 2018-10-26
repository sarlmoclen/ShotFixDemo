package com.sarlmoclen.demo;

import android.app.Application;

import com.sarlmoclen.shotfix.ShotFix;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ShotFix.hotFix(MyApplication.this);
    }

}
