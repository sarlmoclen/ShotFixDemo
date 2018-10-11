package com.sarlmoclen.demo;

import android.app.Application;

import com.sarlmoclen.shotfix.FixDexUtils;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FixDexUtils.loadFixedDex(MyApplication.this);
    }

}
