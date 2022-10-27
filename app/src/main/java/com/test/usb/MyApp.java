package com.test.usb;

import android.app.Application;

import com.test.usb.utils.CrashHandler;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;


public class MyApp extends Application {
    public static CH34xUARTDriver driver;// ???CH34x??????APP??????????????????????????????
    private CrashHandler mCrashHandler;

    public static final String DIRECTORY_NAME = "USBCamera";

    @Override
    public void onCreate() {
        super.onCreate();
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext());
    }
}
