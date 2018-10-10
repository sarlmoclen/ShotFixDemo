package com.sarlmoclen.shotfix;

import android.app.Application;
import android.os.Environment;

import java.io.File;

import static com.sarlmoclen.shotfix.FixDexUtils.DEX_DIR;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        File file = new File(Environment.getExternalStorageDirectory(), DEX_DIR);
        FixDexUtils.loadFixedDex(MyApplication.this, file);
    }

}
