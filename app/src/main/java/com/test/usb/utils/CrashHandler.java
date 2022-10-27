package com.test.usb.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.test.usb.MyApp;
import com.test.usb.UsbPointCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;



public class CrashHandler implements Thread.UncaughtExceptionHandler {
	public static final String TAG = CrashHandler.class.getSimpleName();
	private static CrashHandler INSTANCE = new CrashHandler();
	private Context mContext;
	private Thread.UncaughtExceptionHandler mDefaultHandler;


	private CrashHandler() {
	}


	public static CrashHandler getInstance() {
		return INSTANCE;
	}


	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}


	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// if (!handleException(ex) && mDefaultHandler != null) {
		// mDefaultHandler.uncaughtException(thread, ex);
		// } else {
		// android.os.Process.killProcess(android.os.Process.myPid());
		// System.exit(10);
		// }
		System.out.println("uncaughtException");

		Log.d("CH34xUARTDriver", "调用完毕");
		new Thread() {
			@Override
			public void run() {
                if (UsbPointCommand.getInstance().isOpen && MyApp.driver != null) {
                    byte[] to_send = UsbPointCommand.getInstance().toByteArray(UartUtils.SLAM_OFF);
                    int retval = MyApp.driver.WriteData(to_send, to_send.length);
                    if (retval < 0) {
                        Log.d("CH34xUARTDriver", "写入失败");
                    }

                    byte[] to_send2 = UsbPointCommand.getInstance().toByteArray(UartUtils.NIGHT_OFF);
                    int retval2 = MyApp.driver.WriteData(to_send2, to_send.length);
                    if (retval2 < 0) {
                        Log.d("CH34xUARTDriver", "写入失败");
                    }

                    byte[] to_send3 = UsbPointCommand.getInstance().toByteArray(UartUtils.SHIMMER_OFF);
                    int retval3 = MyApp.driver.WriteData(to_send3, to_send.length);
                    if (retval3 < 0) {
                        Log.d("CH34xUARTDriver", "写入失败");
                    }
                }
			}
		}.start();
	}


	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
	 *
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return true;
		}
		// new Handler(Looper.getMainLooper()).post(new Runnable() {
		// @Override
		// public void run() {
		// new AlertDialog.Builder(mContext).setTitle("提示")
		// .setMessage("程序崩溃了...").setNeutralButton("我知道了", null)
		// .create().show();
		// }
		// });


		return true;
	}
}
