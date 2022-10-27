package com.test.usb;

import androidx.appcompat.app.AppCompatActivity;
import cn.wch.ch34xuartdriver.CH34xUARTDriver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.test.usb.utils.UartUtils;

public class UsbPointCommand extends AppCompatActivity {

    public Button bt_2d, bt_down, bt_slam, bt_up, bt_night, bt_right, bt_confirm, bt_left, bt_shimmer, bt_open_device, bt_confirm_device;

    public static final String TAG = "cn.wch.wchusbdriver";
    public static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";

    public boolean isOpen;
    public int baudRate;
    public byte stopBit;
    public byte dataBit;
    public byte parity;
    public byte flowControl;

    public byte[] writeBuffer;
    public byte[] readBuffer;

    public Handler handler;
    public int retval;
    public int brightness = 3;
    public boolean is_2D;
    public static UsbPointCommand instance = getInstance();  //单例类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_point_command);
        initUart();
        //初始化控件id
        initView();
        initData();

    }
    public static UsbPointCommand getInstance() {
        if (instance == null) {
            instance = new UsbPointCommand();
//            UnityPlayer.currentActivity.getFragmentManager().beginTransaction().add(instance, "UsbPointCommand");
        }

        return instance;
    }


    public void initView() {
        bt_2d = findViewById(R.id.bt_2d);
        bt_down = findViewById(R.id.bt_down);
        bt_slam = findViewById(R.id.bt_slam);
        bt_up = findViewById(R.id.bt_up);
        bt_night = findViewById(R.id.bt_night);
        bt_right = findViewById(R.id.bt_right);
        bt_confirm = findViewById(R.id.bt_confirm);
        bt_left = findViewById(R.id.bt_left);
        bt_shimmer = findViewById(R.id.bt_shimmer);
        bt_open_device = findViewById(R.id.bt_open_device);
        bt_confirm_device = findViewById(R.id.bt_confirm_device);
        baudRate = 115200;
        stopBit = 1;
        dataBit = 8;
        parity = 0;
        flowControl = 0;

    }

    public void initUart() {
        MyApp.driver = new CH34xUARTDriver(
                (UsbManager) getSystemService(Context.USB_SERVICE), this,
                ACTION_USB_PERMISSION);

        if (!MyApp.driver.UsbFeatureSupported())// 判断系统是否支持USB HOST
        {
            Dialog dialog = new AlertDialog.Builder(UsbPointCommand.this)
                    .setTitle("提示")
                    .setMessage("您的手机不支持USB HOST，请更换其他手机再试！")
                    .setPositiveButton("确认",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    System.exit(0);
                                }
                            }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
        writeBuffer = new byte[512];
        readBuffer = new byte[512];
        isOpen = false;
    }

    public void initData() {
        handler = new Handler() {

            public void handleMessage(Message msg) {
                String mess = (String) msg.obj;
                if (!mess.isEmpty()) {
                    if (mess.trim().equals("AA010155")) {
                        Toast.makeText(UsbPointCommand.this, "success", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        //打开流程主要步骤为ResumeUsbList，UartInit
        bt_open_device.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!isOpen) {
                    retval = MyApp.driver.ResumeUsbList();
                    if (retval == -1)// ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
                    {
                        Toast.makeText(UsbPointCommand.this, "打开设备失败!",
                                Toast.LENGTH_SHORT).show();
                        MyApp.driver.CloseDevice();
                    } else if (retval == 0) {
                        if (!MyApp.driver.UartInit()) {//对串口设备进行初始化操作
                            Toast.makeText(UsbPointCommand.this, "设备初始化失败!",
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(UsbPointCommand.this, "打开" +
                                            "设备失败!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(UsbPointCommand.this, "打开设备成功!",
                                Toast.LENGTH_SHORT).show();
                        isOpen = true;
                        bt_open_device.setText("关闭设备");

                        new UsbPointCommand.readThread().start();//开启读线程读取串口接收的数据
                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(UsbPointCommand.this);
                        builder.setIcon(R.mipmap.ic_launcher);
                        builder.setTitle("未授权限");
                        builder.setMessage("确认退出吗？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
//								MainFragmentActivity.this.finish();
                                System.exit(0);
                            }
                        });
                        builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub

                            }
                        });
                        builder.show();

                    }
                } else {
                    bt_open_device.setText("打开USB设备");
                    isOpen = false;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    MyApp.driver.CloseDevice();
                }
            }
        });

        bt_confirm_device.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (MyApp.driver.SetConfig(baudRate, dataBit, stopBit, parity,//配置串口波特率，函数说明可参照编程手册
                        flowControl)) {
                    Toast.makeText(UsbPointCommand.this, "串口设置成功!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UsbPointCommand.this, "串口设置失败!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt_up.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isOpen) {
                    brightness = brightness + 1;
                    if (brightness > 5) {
                        brightness = brightness - 1;
                        Log.d("brightness======",brightness+"");
                        Toast.makeText(UsbPointCommand.this, "已是最高亮度", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        byte[] to_send = toByteArray("AA02010" + brightness+ "55");

                        int retval = MyApp.driver.WriteData(to_send, to_send.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                        if (retval < 0){
                            Toast.makeText(UsbPointCommand.this, "写失败!",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Log.d("brightness======",brightness+""+"AA02010" + brightness + "55");
                        }


                    }
                } else {
                    Toast.makeText(UsbPointCommand.this, "没有打开设备!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt_down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isOpen) {
                    brightness = brightness - 1;
                    if (brightness < 0) {
                        brightness = brightness + 1;
                        Log.d("brightness======",brightness+"");
                        Toast.makeText(UsbPointCommand.this, "已是最低亮度", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        byte[] to_send = toByteArray("AA02010" + brightness + "55");

                        int retval = MyApp.driver.WriteData(to_send, to_send.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
                        if (retval < 0){
                            Toast.makeText(UsbPointCommand.this, "写失败!",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d("brightness======",brightness+""+"AA02010" + brightness + "55");
                        }


                    }
                } else {
                    Toast.makeText(UsbPointCommand.this, "没有打开设备!",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });
        bt_2d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_2D) {
                    is_2D = false;
                    byte[] to_send = toByteArray(UartUtils.UART_3D);
                    int retval = MyApp.driver.WriteData(to_send, to_send.length);
                    if (retval < 0) {
                        Toast.makeText(UsbPointCommand.this, "写失败!",
                                Toast.LENGTH_SHORT).show();
                        is_2D = true;
                    }

                } else {
                    is_2D = true;
                    //否则的话是3D,就切换到2D
                    byte[] to_send = toByteArray(UartUtils.UART_2D);
                    int retval = MyApp.driver.WriteData(to_send, to_send.length);
                    if (retval < 0) {
                        Toast.makeText(UsbPointCommand.this, "写失败!",
                                Toast.LENGTH_SHORT).show();
                        is_2D = false;
                    }

                }
            }
        });

    }

    public void onResume() {
        super.onResume();
        brightness = 3;
        if (!MyApp.driver.isConnected()) {
            int retval = MyApp.driver.ResumeUsbPermission();
            if (retval == 0) {

            } else if (retval == -2) {
                Toast.makeText(UsbPointCommand.this, "获取权限失败!",
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            byte[] to_send = toByteArray("AA02010355");
            MyApp.driver.WriteData(to_send, to_send.length);
        }

    }

    @Override
    protected void onDestroy() {
        isOpen = false;
        MyApp.driver.CloseDevice();
        super.onDestroy();
    }

    /**
     * 显示键盘
     *
     * @param et 输入焦点
     */
    public void showInput(final EditText et) {
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }


    private class readThread extends Thread {

        public void run() {
            byte[] buffer = new byte[4096];

            while (true) {

                Message msg = Message.obtain();
                if (!isOpen) {
                    break;
                }
                int length = MyApp.driver.ReadData(buffer, 4096);
                if (length > 0) {
                    String recv = toHexString(buffer, length);
                    msg.obj = recv;
                    handler.sendMessage(msg);
                }
            }
        }
    }

    /**
     * 将byte[]数组转化为String类型
     *
     * @param arg    需要转换的byte[]数组
     * @param length 需要转换的数组长度
     * @return 转换后的String队形
     */
    private String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }

    /**
     * 将String转化为byte[]数组
     *
     * @param arg 需要转换的String对象
     * @return 转换后的byte[]数组
     */
    public byte[] toByteArray(String arg) {
        if (arg != null) {
            /* 1.先去除String中的' '，然后将String转换为char数组 */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            /* 将char数组中的值转成一个实际的十进制数组 */
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                /* 将 每个char的值每两个组成一个16进制数据 */
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[]{};
    }

}