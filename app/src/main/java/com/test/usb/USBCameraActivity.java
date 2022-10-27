package com.test.usb;

import androidx.annotation.ContentView;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;
import com.test.usb.bean.DeviceInfo;
import com.test.usb.utils.CrashHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class USBCameraActivity extends Activity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {

    private CrashHandler mCrashHandler;
    public UVCCameraTextureView mTextureView;

    //    public UVCCameraTextureView mTextureView;
    public UVCCameraHelper mCameraHelper;
    public CameraViewInterface mUVCCameraView;
    public boolean isRequest;
    public boolean isPreview;


    public UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(UsbDevice device) {
            if (!isRequest) {
                isRequest = true;
//                if (mCameraHelper != null) {
//                    mCameraHelper.requestPermission(0);
//                    mCameraHelper.updateResolution(1080, 1920);
//                }
                showShortMsg("device = " + device.getDeviceName() + "---" + device.getProductName());
                popCheckDevDialog();
            } else {
                showShortMsg("isRequest 是True");
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                // initialize seekbar
                // need to wait UVCCamera initialize over
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }
    };

    public List<DeviceInfo> getUSBDevInfo() {
        if (mCameraHelper == null)
            return null;
        List<DeviceInfo> devInfos = new ArrayList<>();
        List<UsbDevice> list = mCameraHelper.getUsbDeviceList();
        for (UsbDevice dev : list) {
            DeviceInfo info = new DeviceInfo();
            info.setPID(dev.getVendorId());
            info.setVID(dev.getProductId());
            info.setProductName(dev.getProductName());
            info.setDeviceName(dev.getDeviceName());
            devInfos.add(info);
        }
        return devInfos;
    }

    public void popCheckDevDialog() {
        List<DeviceInfo> infoList = getUSBDevInfo();
        if (infoList == null || infoList.isEmpty()) {
            Toast.makeText(USBCameraActivity.this, "Find devices failed.", Toast.LENGTH_SHORT).show();
            return;
        }
        final List<String> dataList = new ArrayList<>();
        for (DeviceInfo deviceInfo : infoList) {
            dataList.add("Device：PID_" + deviceInfo.getPID() + " & " + "VID_" + deviceInfo.getVID());
            Log.e("USBDevices ===========", "   vid : " + deviceInfo.getVID() + "  pid : " + deviceInfo.getPID() + "   deviceName = " + deviceInfo.getDeviceName() + "  devicesProductName = " + deviceInfo.getProductName());
        }

//        AlertCustomDialog.createSimpleListDialog(this, "Please select USB devcie", dataList, new AlertCustomDialog.OnMySelectedListener() {
//            @Override
//            public void onItemSelected(int postion) {
//                mCameraHelper.requestPermission(postion);
//            }
//        });
        if (infoList.get(0).getPID() == 7532) {
            mCameraHelper.requestPermission(0);
        } else {
            mCameraHelper.requestPermission(1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbcamera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        AccessRequest();
        /**------- USBCamera引入 ---------*/
//        mCrashHandler = CrashHandler.getInstance();
//        mCrashHandler.init(USBCameraActivity.this, getClass());

        mTextureView = findViewById(R.id.camera_view);
//        LinearLayout layout = new LinearLayout(USBCameraActivity.this);
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        layoutParams.width = 640;
//        layoutParams.height = 480;
//        mTextureView.setLayoutParams(layoutParams);


        mUVCCameraView = (CameraViewInterface) mTextureView;

        mUVCCameraView.setCallback(USBCameraActivity.this);
        mCameraHelper = UVCCameraHelper.getInstance(640, 480);
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
//        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(USBCameraActivity.this, mUVCCameraView, listener);
        mCameraHelper.setOnPreviewFrameListener(onFrameListener);

        if (mCameraHelper != null) {
            if (mCameraHelper.getUSBMonitor() != null) {
                showShortMsg("数量多少：" + mCameraHelper.getUSBMonitor().getDeviceList().size());
            } else {
                showShortMsg("mCameraHelper或者getUSBMonitor为空");
            }
        } else {
            showShortMsg("mCameraHelper为空");
        }

    }

    AbstractUVCCameraHandler.OnPreViewResultListener onFrameListener = new AbstractUVCCameraHandler.OnPreViewResultListener() {
        @Override
        public void onPreviewResult(byte[] data) {
            Log.d("onPreviewResult", data.toString());
            showShortMsg(data.toString());
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.releaseFile();
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
    }

    public boolean isCameraOpened() {
        return mCameraHelper.isCameraOpened();
    }

    public void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
        Log.d("onSurfaceChanged", width + "---" + height);
    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }

    /**
     * 检查权限 方法
     */
    public boolean checkPermission() {
        //是否有权限
        boolean haveCameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean haveWritePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean haverReadPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return haveCameraPermission && haveWritePermission && haverReadPermission;

    }


    /**
     * 请求权限 方法
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    public void AccessRequest() {
        //动态权限检测和申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//大于Android 6.0
            if (!checkPermission()) { //没有或没有全部授权
                requestPermissions(); //请求权限
            }
        }

        //加 StrictMode, Android 7.0以后，获取文件Uri需要加上这么一段
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }

}