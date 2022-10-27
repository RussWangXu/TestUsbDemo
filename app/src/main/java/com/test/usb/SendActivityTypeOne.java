package com.test.usb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.hoho.android.usbserial.wrapper.PortReceiverListener;
import com.hoho.android.usbserial.wrapper.UserPortCommuManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class SendActivityTypeOne extends AppCompatActivity implements PortReceiverListener, SerialInputOutputManager.Listener {

    private Button btn_open;
    private Button btn_read;
    private Button btn_send;
    private Button btn_usb_camera;
    private EditText et_send_data;
    private TextView tv_receive_data;
    private Boolean successPort;
    private ImageView iv_byte_image;
    private UserPortCommuManager userPortCommuManager;
    private static final String TAG = "SendActivityTypeOne";


    private Handler mainLooper = new Handler(Looper.getMainLooper());

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
//            switch (msg.what){
//
//            }
            read();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_type_one);
        //初始化页面
        initView();
        initData();
    }

    private void initView() {
        btn_open = findViewById(R.id.btn_open);
        btn_read = findViewById(R.id.btn_read);
        btn_send = findViewById(R.id.btn_send);
        et_send_data = findViewById(R.id.et_send_data);
        tv_receive_data = findViewById(R.id.tv_receive_data);
        btn_usb_camera = findViewById(R.id.btn_usb_camera);
        iv_byte_image = findViewById(R.id.iv_byte_image);
    }
//    public void bitmapToString(Bitmap bitmap) {
//
//        String string = null;
//
//        ByteArrayOutputStream btString = new ByteArrayOutputStream();
//
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, btString);
//
//        byte[] bytes = btString.toByteArray();
//
//        string = Base64.encodeToString(bytes, Base64.URL_SAFE);
//
//        Log.e("sdfsdfdsfsdf",string.toString());
//
//    }

    private void initData() {
        btn_open.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                userPortCommuManager = UserPortCommuManager.getInstance(getApplicationContext(), "1A86");
                successPort = userPortCommuManager.openAndConnect(115200);
                userPortCommuManager.setListener(SendActivityTypeOne.this);
                Log.d(TAG, "打开端口号 success : " + successPort);
                Toast.makeText(SendActivityTypeOne.this, "打开端口号 success : " + successPort, Toast.LENGTH_SHORT).show();
                if (successPort) {
//                    receiveThread.start();
                }
            }
        });

        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = et_send_data.getText().toString().trim();
                boolean success = userPortCommuManager.write(command);
                Log.e(TAG, "发送失败: " + success);
            }
        });

        btn_usb_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendActivityTypeOne.this, USBCameraActivity.class);
                startActivity(intent);
            }
        });
        String data = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAEOAeADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDwfcc05EaZ8DGetISvpQrbHDLkEcg0gEZCjlWHIo6mpJpTK+7AHGMVH0FMBCeaAMnAoAycAVcgt8DJ60AFlZm5u4YADl2A4r1qyhisrREQgBRgAVyHhLTQ0z3si8J8icd+5rop/wB7cCCEHexx1oLRchV7+42/8s1PJ/pXR28CxoFAqpYWq2sKoB06n1NX1YHp0oKHYPSpV6A/xL/Ko93rTlPIINAjk/HeiNcWn9p2oy8Y/eAd19fwrzVF3Nz1r3oY5jYAxyfofSvKfFOgNomqb40/0SY5jPZT/dqJIpGPFDnFWUhx2qaCMFBirSQ81i2bKJXSH2qykXOMVKkOKsJFUtlpDI4+Kl2e1SqgFI4PapLK7pxVG8izE3Y1qFarXMeYXz6U4vUmesWc2v3mzQDzSL99qB1rrPLHd6Ud6TuaKAGuOKgPWpzUJ60AMNIKUimZwaB3CUjctanhbw9deJ9UFtApEScyyY4Uf41U0/TLnWNTgsbRN80zbRnoPUn2A5rrvFOuW3g7Sv8AhE9Bf/SSv/Ewu14JYjlQfX19BgetMpF/XfFth4Vtjo3hry3ulG2a8wCE9k9T79B7np5tNqFxLK0k0s8sjnczMxJY+pJ61SWXLDqSa9M8K2efD6wz6XEjSSmVp7lEZiCMcArlVwBjnOcnvQM4nTYp9SvEggSQuTyducD1r2HwjpTWF68ssoMzwBHUKBnHQ8dDVe0FjbrshjVEPV0AXJ/CtLQ7OSDWDKJTLC6Hac9OnFIaOqWlNSJC5GcU7yPUj86Qyo1VZuhrRaFf7wqtLFGc/vAKQzJlH+i3R9ErDgn8xyoJCqOea6O8gCabelSGBjIyK5OMvDps0pzvb5VzQJsytS8MaTqhLfZjbTt/y1g6E+69P5fWuN1nwrfaOHlTNza7cGVFxtPuMnH1r0ayZhbvczDBX5V/2jSW7mdiY8q38ZPQU7geR2fyKXIBwa9F8OXdhLIt4dW8h48fIisXyeAMdOTxyaqa54agu4Xk0tEim5LRgYEn07A1z2n2OpQK0dpARLkby2Mgjp14FUmNK7PWpJ7LVnZE/c3OcJv4EnH5Z9hWYsj6dcsWhBcDGG7VxU39u2Ma3F5MwiU5OCpI9OBXUaH4ks/EyjT5px/aiKTG5GPOA7deTSejuimlsyHUPGaWsqpKDGrdJFTcP5n+Vaun39zfWaXNu8E8cgyGPBHtwK5bxLt0+EtcQF4i211CA4P49KxdE1+O1uzFpMMkEsqnAdyyMQM8qTjtjPXmhzk1oQopPU9NZLqRSGk2E/3O36Vz9/qJErJbyl26NIOM/THWq0fiXUtS04w3VpHayklXaNj8w9geg/E5qTTtOa6lAA4HU46VCnNbspxg9kR2lhPeS8AsT1J/rXR2WhwQgGQeY36CtKzskhjCRrhR19T9a0VjRFyagtFOO2WNQqKFHsMU14sdRkU+71TTrJwt5fW8DEZCySBSR7DrUlvc2Wows9ldQ3AX7xjcNj646UwM9k2t147VltLuvZl9MVq3bpCru7AKoJJ9K5u0uUudUuniYNHhQD+FTLYqO5qd804UzNLmszQ8MzmkOPSmg0vWus4gppOacas2NobiTcR8gP50wH2lsT87Croj2jpVtINoAFWLC28/UII+28E/Qc0hm7azGx09LSKIllHzNnjPf9a3dEsSim5m/wBY/T2FMW2WeYKqfIvJ962UQquBTLJs54FLuweKao496UL35oAnD5FKCQc1EuR1qVT+VIRMjhvkbgGo9Q06LWdPlsrleSOD3B7EUqgVaQlwNpAdenv7UBc8rewn0q+ksbkYdeVbs69iKsJH616BrOiw65aDJ2XEeTHJjlT6H2NcP5MttcPbXKeXOn3l7H3HqKwqRaOmnNPQaI8CnCpQvHelEZrE2Ghc07ZmlAxweKdnAoGQMhzioJ1PkuP9k1dIB+tRypmNhjqKE9SWtDi2G2dh70nenzDbdyD/AGj/ADpn8Vdp5THDrQaO9K3SgRGaiapGNRNQA1zUfenua0/DOk/234hs7Eg+W75lPog5bntwMfUimM7Xw/BH4P8AB82vzKP7QvI/3AYfcTt+Zwfy9K8uljE91JNdSOWkYsz+pPJNeh/EvU/PuYLSMgRLyFHZRwP61n+F9HLML2VARnEQIzk9zSuaLYn8L+GBZut26MJHXILjlF/oTXS/2ysMpSOESwgYJbq3vUlzqEtpJHBBtaUDLluQPanPrVmSE1C0ByPvKOaCblhBZ38GLcrFIy8KelRWeoS6JeJEyybj/Dt4x65/wplzpn260Muj3Kux+YIx2sB7f/XpLS/Dw/YdaBRs7ElIwynHf/GnYVzura++0QK4OcjOan8xsHGazNIiMVokIYPsG3d6+9am07fvD86RoiB5H5xmqU8jhScVdcL3aqF5t8o4YZqRogCS/wBg3xKh5HYlVBxnPQZPSuevYXVbe0CEZ56cZrpkbboxP958frVcEEY/nTQmjmr9S8kVjB/B192qC/uFsIVtIfmf/lq39K25rL7LHNJbB3kPQMclfXHc1l+TDYAT3QD3LcpGf5mgWwyCLyrdXuPkB+6venQ+W0ryxJEJHGCWTdu9D1HPXvVI219qU5lLFUz95uFA9qsrdWNsyxLIbiQdx0BpFJmJ4+maPRo4mlRVllAYKnJHJ/mB3rzuKWG2mSaKWZJY2DKy8EEcgg9q77x3l9OtZQp2CUhuM7TjiuAaURurq7hwcgqcEH1qhM9Ytru28c+FpZioF3Evl3cYAyeOHA98Z+oPpXmenWdxFrqw42vC53n0A6/n/Wr3hbxO+i+Ibe5kVfszDybgAZLRnqT6kcH8MV22u6LDp+uS3MKgrdAOGHQj2qXoVuVLWAyOqgZJOK7PT7RbeFUXr1Y+prF0W2zIZSPujA+tdJHgCoLRYaWO2gaWV1REG5mY4AHrXmHiX4h3N48lrpLtBBkgzjh3Ht/dH6/TpT/iP4jZpRo1u3yjD3BHc9Qv8j+VeehqtImUuiEllkMzSO7M7HJZjkk+9bPh7W7nTtas5YJChMqpIAfvISMg1jyDctTaTH5msWKj+KdB/wCPCm0Rc9m1ti+mXGDyykVg+HojEs4J/iAzW7qxxpcpxk8fzrI0UZgkbHVzWMtjojua2aXNMzSisizw4Ag8jFO6datXqx/bZRD/AKpWwuG3Agd81Vb0rtOMfbwPczrEnVj19BXTWlukaqFHyLwPeqej2TrGH2/vJenstdEIQiKBwBSZSRQaPqTxV/RbZnuDIq5P3V+pqCUei44ro/D9uY4Y3x82C5P48fpQhnQWloYYgCOe59anxzUSzSN2qVMk8imMcFzUipilQrU6hD1pCIgimniLtUoRe1OC+9AEQiNPRdvepMZFJtPv9aYE6Dc25Dhh1H96qWraJb6zADny7lP9XKByPY+o9qsJG2c5IPqOKvR4lwQdr+vY0NX0YXs7o83mt7iyufs95F5Uw/Jx6qe4o7cV6TdWNrfwG3voBIvUZ7H1B7fUVzN74MuoiX025SaP/nnMcMPow6/l+Nc86TWx0wrp6SOaNNNW59P1G2OLjTrlOf4U3j81zUUdrPK21IJSf+uZ4+vHFZcr7G3PHuQikYZ4rft9DTyx5zt5jDouML/jWb/Z0rtObdWmjilMRdV4LDrj6dD7gjtTdOS1JVWL0ucDeLtv5R/tVAfvVc1ZDHqsyn1qmetdaPLluPHWlbpSL1FObpQSQNUbDFStTHHFAyIRtI4RFLMegAzmu++H2ny2Meq6nPFJG6wrFEWGM7jkn8No/OsbwfpZv72Z8Z2bUHHTdnJ/JT+dej3sBtND8lAN0kpIH0GKprQIu7Z5tqkJ1nxcLchmjjUBtv8AdAyf54/Gu7spY4LNykarBCAqgDqfT+VY/h7Slt9Su5b9WEszHaF/u/X/AD0rpZJ7PT4hEbNHjA3bWJwtSWZ03icwQnzbSGUjgLjGahn1Lw9dzrHfWclvIq/6yPoM/T/CnXfiHSEuTDLocT8Alhj0omm8MXUuZ7C5hLAfvEOR0+tUkQ35lhNE8zZcaPfrLsA2rna4q7DG+pobbVbPdLEOJCMH/A1Ug0vTZSJNO1XY4A2rL8p/Pit2PzorVFml82UDBbOc0ikOsf3VwY0G1FGAo6AVrA/LWRa8Xffp1rXX7tItEElZ90flrRZsHGKz777uRSKBmLWscXmJsXJwAckn1+lQMcVm6bfy3TTxyxlTE2AcdR/kfrV8nNMQok7Gs+8treN3vJFeQYGY88Z9farZpRyMEZHvQBiSre6l8qJsh7AcL/8AXqKDSLWykMs9wGc87VHArQktdSutwlZY4QflOQoI+lVrizg2t5+oIpPB8vt9KTRKYy4Gn6lDLaOjMr8Ek45HSvH9ShlS/kieNlKuUVSOuCQf1B/KvWraHSYCFjvJGIPVgev5VV1fR7e4vVvpZoAqpwZbcycf99D6j0PNJF7nnMej/uQGOZGxgjsa9ZSAXXg2z6tJZARZPUqOB+mPyrzi3s2GuwxGR5IwfMVmGCQOeRk45969Q8Or5thc2x6Oh/ShjQafH5NsoxyeTU95eJZWU1zITsiRnbHoBk037qgDpXM+PL423hqSME7p3WLIPTuf0BH41CRZ5bd3kt9ez3M3Mkrl2PuTUQbmml8jAHFArUxJg3rWh4fUHxDYL2Nwh/Jgayi5AwBW94SsLiXxFZStC4jV9xdhgcAn+lDGtz1TUf8AkGv7kfzrL0xdtrx3Y/zrT1Q7dNHuwqhpo/0ZawkdES2B606lxikNZGh43eW721y8UilWU8g1Jptj9pk82QYiU9+9dheaY11K090TK2fl8xi+0ZzgZ7VFFaqUVdoCj+EdgOg/Guy5yWHWUQUl3XBI4A7DsKnbgDuTxQpCjd2HOaZENwMrcA9AewpDIpVxnkk+tdvpVt5NrGrDoij9K5GGPzr+1jx8kkyLj2J5r0JVVHYY6AU0A0KOwpwAPBp+FPSlA9qAGeRu6Gj7PIvTOKnXPaphIR70AVEaRD8wqykoYdKlDRtwy4qQW6NypGfSmA1fbrUqox6U0IUOCpqRHXdg5FAh6x+uCamVcdsCkUD1AqQD6H60CFU44Bp24A9B+FRkkfwj61VlvggIXBPTimIty3axYBJLHoKx7m8kuWILHZngUTOxGCcyOOfYVWuZhY2hm8vzZT8kMIODK56KP6nsASeAaQyveyyl002ycC+uFyX6+RF0Mh9+y+p9gabrOs2Hg3QI440QMq7La3B+8R3/AFyT7+poe4h8L6LdanfyRy3knzzunHmPjCovfaOg/EnvXjWo6jfeIdUku7py8jnAA+6i9lA7Af55NbUaXO9diZM0dWme6vUupPvzxJK2BjllBNUm61pazayWc9tDMjJIlvGrK3UEKBzWc3WspLVmbHL1FObpTV7U9ulSIhIpjVIRSFeKYHpPwvt0ksp5CAGE7/jhE/8AijXY3ccct3DE5ACqW5rxnRPE+o+HXc2TRlGzmOVcrk454IPYd+1dfd+IrTxLr2lwWbMU3rNNuUjbsG7bz15Hb2pt3HHQ6ptPJdpSypGDx6tWdqd5oNuxW/a5kkbrGgI6ccH/AOvS2bXV/qhuWDGCMkKf4QKln0e0aZ5b69t1Lh1CBFcgNxxuzg+45FIrUzm1Dw3GEcaTM+VwCxB6djzT/t3h+52k6TMxJxw/P86sfZ/DtnEFFvPcgHOSe/6UJq2mxkC30WLGerH/AOtTI16kkMOiO6JFbXMTEhQM5HJ781rSDLqOwyf8/rVK21CWa5WMaPHHGT/rVQnA9c1eYHeT6ikaIbb/APH4BxitfKIuWdVHuayFCK5dwT9Kzta8QWOiWDXU8DyKCAFBGSTQM3pLuyz/AMfUf4HNUrqaCSMiOQMfYV5/cfFSwlbKaIXYcAu4H9DVb/haMe4Y0RIx6iXP6YFKw7ncpsTIUAZOTgdTTwc1xVp4606ZwZS0RJ6MtddZXMV5brPC6vG4yrKcg0wLFIOop2KQDkUhlK9tLaWQvcX8cPA+R6rNp+nuuf7RVs85ANa97aaetkk16QWmJjVF5faCDn2GeM59R61mpaaPt2RXkkfOQJFPH40EvconSbIyZi1CMsf4TjmjVIJItJdD8xUdR6U660JpQXtLmGXnsap6oLq18O3cYhZpsbVX/e4z+GaktGDYQ7tTMp/hhx+Z/wDrGu48OZWdB6hq4PwrYXNst29yhVnK4yfTP+NegaEm24j9gT+lA0OlGCR6V538Q5WkewtVYcl2IP4Af1r0OU5ya8v8byD/AISS3DZKrCvAP+03/wBaktynsYdvowkwZJjj0Uf1q1d6ABah7QMzryyk5LD2960rOQeWpSCMY678vn+VasEk5yVYqh/hVQB+HGf1qHWS21BQ7nEaZpd1qd0I7eFnC4LkDhR716Vo2mSWs8e8Kvlg8Z56VT8uIxbLiRCmc4mfdz+Oa19JvbSQpbR3MLygE7UYHiiNSUnqrIOVLqX9YH/EuiHq4/kaqWa7YFHtVzW+LO3X/b/pVWHhFHtSkaRJ80h6UopDWZZTvbdvtMcigNE2VYemR1rPubcJliMZ5NdMkJZShxj9ax76JgfLZckHHHeuswZhyfOwT/lmOXHr7UhIZ9uDtzzUkimMkKDuzxn1piITkD86CCxEypcwzuPkgkV+PYg8V3Ubhp5SfavNdX1JdMtcpgt0Rf7ze/sP61u/D7WZtUsbiK7mMs8MnUjohHH6hqEFzswo7U/bTguKcBQMjCnpUipnqKcFFSAccUwBYAR1xUqQADhqiyw5xQJ9vOGoEy0nmrxuBHoaflSPmQf8Bqut0Om0/lU6TI3QUAG1P4WK/UUjblHMwx9KexGORVS5ZPKO/wC4OSAetAitc3oPCs7knAzwD/8AWpEGAZHGcdB/SoLdGuJjKwA7KPQVoqFAzjp0/wAaAK8cTMxd85NRXS28DNfXcm2O3Q7QTwo7t9T0+nTGTmxc3UFuF86ZI93TJ5P0rhtd1RdevHs7MtNa25BaJSP9Icc4PPCj1PGfXAzUY3E2c/r/APbXirWLSMxtBaXEm20ic4Cjj5mAyehBz78V2nhbwlpGnXa6hI4e2tlLRyygKshA+aQ56AEEjsBt5zmkj0mFRFcai7GMPundid0x2kCMAZJHzdB9OSSa6BppL+SGW4hWOCFg8VscMAw6FscEjsBwDzyQCNJVNLRFbuec/EB4p/Ejzw52OBjIx2rlmrq/Hg3auj7Qu4ZwK5VhWJEtwWnt0pi09ulBAw0dqO1KooAryCui8BRLL4nTeeFidv6f1rClWuh8A5/4SQqOrQOB9cigpHoFprBv79IYQEtVyFUDGcVCmg3VxPLIwWGIyMd8hxkZPap7X+zdHnjso/3t07BXlP8ACT/KszWv7SvNZnsrd5mkjw0aZJVlIH4DBzRa472RoNHolgP388l04HKx8D86hi8RozeVpmmwRSE/KXXcT+VLFoS2No1zrt0NgAzFEuSPxpF11Agg0K0it2PGXXMh/p/OqSIbZdtz4jmYTXc0dtbKQzByEBH4VpyABgRyOnHSsKLSr9j9r1rUpIkPIj3bmb6Dt+FbFtPDd25EKSIsfyjfjJHY0mVFjnX5TXmHxLv8G2sgfWVh+g/rXqI+YYPUda4fxT4Fm1vUHvYrwKxUKI3TgAe/50jQ8kiAJGTgE8mrjQxSCSRAQoJwR0GB3+tbV74E12xyVt1nQd4mz+nWsG4tLm1cpcQSRMOzqRQFiIxMJFQcscYA9+1e6aNZiw0m1tv+ecYUn1OOa8k8KWLaj4ltUYFlRvNc+y8/zxXtqJhQKAGYpQvIp+2lIUDDSrEW4DN0zSGVLu5NuxS5sElj/hJGD+dZ4OjXz7Y5JLWbH3X6Venur/TSI7hBPAem/kH6GmXOj2t28M8avaTyx+YkcowJFOQCPUe9Mi7bMe/0e9tR58BMiDkPEealhvJJNLMlw+4jgEgcVBNc6hoc5X5lUfwN91ql1q4J0Vr2OELjbJJGO65AbHvgmoNEZ9m8kyvJM252brgDt6Diun0pdkU0x7JgfU1zlnFhyAcgHrXUovk2Ecf8ch3H6UloVe7KkvSvONfcjxWHVtkqBfKkBxsbqDXpE4wprzXxWitfzsSqPhWUk43DH/1qFuN7FN1uUl2tcgAnnDhc/lin7FIw8wcH+/IW/mTWZJeSSRhWlhwOgAVcfkKiF/dxrtju5FUfwic4/LNWSblvHa79sbR7/wC7HHz+groPDcsD6q8SSbpFiJKngjkDp+NcA2o3rKVN1IV9N5Irpfh+ztrs5Y5H2du3+0tJ7DSO6104S1Huf5VAnAH0p/iI4NqPY/0pinp9KxkaxJRQaaDSk1maF6BgzBUBVh/yzbkfgadfWqTW77ATKoyKmt4ZAvETscY3dTirYgCrmT5frXWYs4We0lJ3tx7Cs+5kjtIXllfbEnJbux9BXQa9rWkWd41u90PN25ZACcH047+1eaa9rLapOBGpjtkPyoepPqfegzZR1G+k1C7aVxtUcImfuj0ro/h1f/ZfEy27E+XdIUIzgbhyD+QI/GuTxUtrcSWd3DcwkCSJw6kjuDkUyT6NAI4Pb9aeBVPTdQj1bSra+t+VkjDDnOPUHHcHP4irqEECgtChacFPanAU8CgBFQ5qYZA+6PypAtOwfagQZY9lH4ChmI6tj6UhIAqCSQYO2mIJJOtUZiZ2CD7v86fI+7Oef6+1PiXYu4jk9BSAeqLHGF6Z61X1HUrXS7Nru9nSGIcAucbjjOB3J4PA5pL/AFC00q0a7v7hYYgcZY8sfQDufpXlGr63L4s175LeVo0VltLcLvPT72BxnOCe2B3xyxG7dyt4u1AQ6WzzKvzS3ZDKiKewBGf/ANX410tjolj4fsvKhhFzcMMnc+zdgjJJ5wBntn6EnnH0VH8G6BNLrVzFHGXLx28Z3Pk4yo5wT06cDqTjpQ0NtY1zxQPEF3F5Gn7WiWInlkwdqjuTuwSeOh6dKpz05VsJHXQwvNO15dNvcnC8YVB/dQdvc9T37ATX+p2WkWgudQlEasdqIOrcZwM1cSCdoJrkxBjDE0giBwAorzi+TxLqNy0smrQoCTtjRPlQeg46VKQSkUfEGsHXWjvdioPMKKinO0ADAz3rFkHOK19Utby2sYlvrr7TL5mVcDGF44/MH86zLldsvSlYzZCtPPSmrTz0oJGnpQvWnY+WkXrQIa44rX8Hym28U2ZzgOWQ/iD/AFxWW/SnWk/2W9guBnMUivx7HNMaPUrTRWkvZru5l8i3jlLBz1bntV3XL++WztJNJaNY7vhpjhWAxkcnAHGetZuvXMsj2jIxNs8YKqvr6/qK3NB0q4h0P7Lqccbr5jPDESchDyA34k/higoy4dLutTRwWaCzcAySPcLKGI75Hfipmkt9HiMOj24aYjDXMgyfwFLc3kmpSusBSSKB9ohG6Mofde/69KtiKLTIBPdx+ZcHlYFOcfWmQZUGhyXbnUNYuZBGT0b7z+w56VqW088k6R2kaW9nH1THBHqxqGL7Zq91ukXZGv3mY4CD2FTTuJ9tlZg+Vnn1c+p9qYJ22LfyTIJYJAw5AYD9CKQSrnbINjeh7/Q96gMjWsqWtrtZgf3jEZ3H0+lWi8E0z2/V16jHyn2qbGikIUU9hVa4sbe4QpNCkinsygirAtOMxSMAf7jBh/UU020xG0Tyg+oRc/ypFGXZeHtM0+7e6tLSOKZl2kr6fStAkAlRyw6gdvr6U8WjlRvklfA/iYLn6hcA03dGtrI8BVxF1QDAFANirGWPYntz+gqsTBe/6PMnlSchG7fQ0XYF1bLe22UeI4Kg5xSyqup2RuIsCdB86jv707EuVylFdT6c72V7H5sI7N3X1FN1GyaWFbu1uJJoUxtBYkxj09hVi2uY9Tj+xXZAnUfupD1PtWfHczaNeMsgJGcSJ2YUmCG22oRakrafqKb+0cmOQaL6x89xEQ21FGB0B7g1beztIZzqNqwZJV+VB2Y1oXZeBozPGA7xKy49O2ag1Rh6Xp5M21vugksfYU9Zpp/EMpyfJEYCr2HPFb1nabbdiRjeMkn0rMhVRcyuByxB+g7U+gnukFyPlNeW+KHvH1adDbJJAmNjNGOOATz9a9TuehrzHxPqQi1Ke1CsDxlvqBUrct7HKmylb5hbnnng0xrOUdYWGPeuyt9f0qOxjg8yRSigZaM8/lmsme/tpmm2yg7jxkHpVkGD9klB/wBQ/wCtdr8O7Zl1C7laMqRGFGc9z/8AWrIWS1LZ3xfiRXZeDRGTcGPb/B93HvSewReppeJWxdWyf7Of1/8ArUCk8RjdqkPOAEX/ANCNGeaymbxHg0E03NGazNDsNQvIdOtZLm6mEcSDJJNeO+IPHV/qF+TZSNBbL9wY5Puat+OvG1r4kht4LBJFjX55S6BcnA+Xqc47+449a5HT9MvdX1CKysLaS4uZTtSOMZJ/+t711WMHK+xBuaWQyOxZiclmPJNRTAM25eh5rQn04wlo5LmFAo7NncfwqgBlCM52nrQnchq2gwDK/Sm4qSNeSPalZcUyTtvh14qTSb06bfSFbS4b9256RueOfQH9D+Jr1mVTbvu/5Zsevoa+bcc16v4D8dw3ECaNrkyq6jbBcyHAcf3WPY+h79OvVjTPQEYfjU61VltJYOYfnj/unt+NMF0ADvDpg46Z/lQUX+e1IWA6kCs9r61Gf3u4jsCSfyphuZZvlgTYP7xGT+FAFyaaONdznj37/Qd6qGVpPmIKJ79TTUgCkvM5aTocnJ/+sKq6prulaNA017dIGUcQpy54OML74PJwPekIvxxFiGYYHZa5TxF4+stLL2+nbLy7HBfOYk/EfePsPz4xXKaz4y1PxGzWtoptLE8MqnlgePnbv34GBz3xmsyHTo4SGOXb+8amU0iowbEd7nW7s3msXsrdOMbnIz0UcKo/LrwDW7Yag9pH9i8PaYn2mUhVaZtzP7np7nrgVlMAi5PAFQw3k8U3mwSPE4BCujFWU4xkEdDUKbZbppI7nR/h/HaAal4hvDc3hUNtkbcFx6k53EAdP50sPxE8MQah5HmXboDtW4aECNfw+9j/AICKr6P4jsmFpDqbSOoUJcPKu8OMYYnqTn+teearp0f2y6MEYSJJPkZclSGyV698D9K106GGvVHu8eqwmG4uIZkkhnsp0V4zuBzG2MEe+K4lJPmrkPA+vSaPqzWNzKVs7qOWEgnhHeNkVvbkgE+nPauhW4w3WrRk73G+JdpsofXdz9P85rAvgPNB/wBkVq61P5ltEoPc1m3w5jPqgpMGU1HFO7UqenY0YpEkRnUAqQQdpPPFVlueNwViEPPzCrvkRurbl5JznNVZ1it0Hy/K7AMSSaAFa5DYAUlueM9cVGJgBlVOB0yfYmnecgijZQm/n8KmSJCikxrkjPSgD1PwBqEWqaLAZwrXFkSoB7eh/L9a17LxSt54gl02OOR2jZgTj04P615f4V1n+xNaSRji3k/dyjsAeh/D+Wa9Wv7mDQ7J7uzt4/tt2ceaqD5R6k9z/n1plplnU7aHSftGqW1ohu5eDI54T3rG0wXeqTYlwx6vNuBFP8JXVxI81hcbrmCYs+X5KE9c+oP8/rVrXIrnSbKO10222WxO6SRTkk+h9qEKSGX16Qv2Kxj/AHK/ekJxvP8AhViNDpdnvkYG7lHAx9xaj0uMC1/tC5iKhfuRn+JqgSC41a//AH7ABjkqvQD3NMjUngZbSye+blm+WIep9abZo0FhLO5/eP8AKD7nrUWoXK3N6kMAykf7uFR+RareokQJHbg5ES8+7GgLjBmLTgc4Lv8Ayp1zK8aWg3MCQCeetLfjyo4If7iZP1NQatO1tc2gXGQgP40DeiJ5mEWsocfexk57EYqvA4ttdktn4jkyhH15FO1u5S3miAT96QrBvQDPFVtezFqEVwnBaNWB9xQkDYttMNO1l7OX/VyHYc+/Q1DLO+harg/6vPT+8ppPEwDTWN6n3Z0AP1H/AOurOsiC60aG+mRm8r5HK9R70it3YzPE1o0LQ3Vk3yTMGjZex6//AF6vTRR6/YRSq4W5iba/uKbprx6npVxpysf3eGgZxg4/zmrelR2tvd/2fGBK4GZn9/T+dQzRIm05rdoJEjRXiiITOcAkEZ57V1V5o9vrMlteI+1QgGzA6fh3HTFeV65ptzdi3gstUa1tI1yFjBIkJ/iOCAf/ANfrQuv+IfDun3NwPEDzAMshV7dTwP4FySqA9yFzQVrc9A8TodMhiSLlZQeT2xj/ABrlrUne2eTjP61RsPH8/jNntru0jhnhDyx+SDtCZUYJJOW6dgKvW4xJj1BqWNE1yvymvI/GiGPXycffjVv5j+leu3HKZ9q808c2ubi3uAOMFCf1H9aS3KexxWfakyPepSlJsqyBgIr0L4bJ8l4/ZnQfln/GuEAjx905+tei/DmMf2fOw73BH5KP8aT2HHc09fbOrD0UID+eaAeKqa05l1yWNQ7MJF4UE8Bcn+tWTlG2ntxWUzaA8GlpoNPxWZoeWafYS395BawRmSaVwiIDjJPv2r6q8E+GND8LaXCtvBbC+MQWe5C/NIcDPJyQCRnHSvLvB2l2nhQ/bZba2vNQZdoadCyRDvtGRye5P4Yyc93a3uo6tZSG00TTggOPMW3RB+G84OPbNdZzHmuveBbCO51SWESPGt3IoC8iNTu29B64H415tfaVd6XLJFcwtGckAsOpBIP4ggg19K3GltY6fdLfXGhLBcKyTCW9+yoFYbT9xODz1GOvrXj3xN8VS+I9Wit2Nm8VluVZLQEo5J5IY8kcDn2yOtTFNM0qSi4qy1OAjX56cyipo49qliOvSmMhJ6VZiVyKAKtR2ssrhY42dj0CjJrQt/Dmp3DbUtSp9JXWP/0IikFizovjXXtDRIra8MlumMQTjeoAGMDuB7Aiu40b4m6deRhdbtltpxn97EhaNvw5YH8+nXtXBf8ACN3IZFaWFS3UEtx7Zxj9a6DT/h+t1axzy6kF3ZyiQ57465/pRe4WsdZN4/8ADin/AEcmY+0e3/0PFYd98S7jdt02wi68M5LZHuABj8zUqeAdHjA3z3bnv86gH/x2o5/D1npUUlxZtKASAyMwIx0yOM/rSbsrlRV3Yxpdd8VaqzF7lrWInoihMfQ/eqAaMJX8y7mkuHzn5jgflWsGzTs8dK55VGzpjTiimLVY1AVQo9AKil2RAljVqViAazpcsxJqUU9ClO7TN8wwo6CmqvNWPKyeaeIgKu5FiEkqKTcXQo4yh6iptmTntT8oB05p3FYwdSszEFmjB25x9DXbw6VNJDG/mcsoJGOlYUojnURShvLLDdt64BzxXomn6XqOo2sd1Z2FzPbP92SOFipwcHBA9sVvB3WpzVYWehx+raZNBbLIzbgM546Vm3vMcB/2BXrPiWzF1oEWnp4Zl00+YWFxKWdnIRjtLFR1xnGccdK8qulzHEO4XFUYtWKKClxzUscZoZCGIpCEUfKapXqK8QU9c8DOK0ljO36ioihzTEZaWjGLO1uOuRVyJSsSBhyBiraDacU2RMHOKAKTjk16Z4S1m31LT00K+cCTy/3DE/ewOn1H+elecFealkvRauEeN0mTDRSJwf8A63PepbZrTSd0z1rVd2gaMNPs2dbu5XMtyBjHsD/n+Ro8G3mo3FvNp2pwpPYxx/LO7A7R/dPqP5fyyPDHjWz8RW0ej66Uivj8sM54WU9gfRv0P1ODv67ZNZaGumQxyCCVczzKcEk9sjt+n8qPMPIvavpt48ltcWZWS0jXAhT0PcetVbm6i0nTV8x/KuLocccotY/gez1XS725kfUSdFjjLNEwyA2c5APTv0PPetyW+8N+ImSG8eO2vSdqo8ih/bB79ehqkyZR7FXQ/sm2W9Q7oYBkuQeT6c0y1lGp6wkbHod5XHX3Nal3oN1baKljphQqGLOWOC30qjo+nXWmW97c3MLJMF2p369+KdyOV6Ihu72O91IxxnIMoTOOwOKj19t+qqoBAj+XPbgD/Gl0i3MurW/ykKhLHI68VU1G6WXVMYAaSXHXopb/AOvQKzaJ/ErD+0rVO5hX+Zo8RShdP06RvvOpXj14qXXbW5m1fzILaWUxwKqBV6tyevTvVubw/e6no+mROBbtCuZPN5K8D04/Wlcrld2Zepkz+D4ZM/NBOgz6ZbaP5irHh+GS+0q7gnidbefO13HX3x+ANbNnpmn6NpN1JNcG8g8wSShiGAbjAHbsOK0raZZ7YSeUEXaSAPSpZqonN6Xc2KX/ANgsI/MRRukm/vH2rlNc8QDw1fz2mkIk948heeQglY0zwOO/I/z03raFNIjdbZ991J9+bHT2FUVjmgiYJc3AALMoEpAXPXAGOv61ULJ3kN36EsrGSOF2ADGIEgetc94pwNBuB3JUD/voV0kw2iIeif1Nct4wJ/suJR/FOAfptaoGc/4RvBY+I7UsSI5iYWx33cDPtuwfwr1DaY2DY6GvHPL4zXq/h3VF1zTFkZgbpMLOuRnd/ewOx6/mO1KSBGg53RkDt/LtXN6zp6X9tJBIOvQ+h9a6J1ZG24+YdB/eHp9aqzwrKu5OfaoNEeQX2nXFhKUmQgZ+Vh0aqZWvVrmzWRWSRAynqCMg1jTeGbGQkiIoT/dYimpdyeXscFtr1LwDam30dCwwZGaTB/IfoBWTb+F7OKQNsaT0DnIrstJh8pSPahyuNRsctf3Kpq9xPkFop/TOMHFaxUSjd681W17SDHrAuoZBsuUJeMr/ABDHP6j/ACaljUqigE9KzmaQQ7YQakVaBu71NGmTUGhyOu+JV80RWsx2hiSI3PTsCwxn8Kyl8U6hFbiG2PlLkksOWOfc1L/wid6x/wBdEP8AgR/wqWPwk+f310oPsC388V2X0OUw57y4uZTLPM8sh7sxP61oaHor6peIbhnitd37yYJuIHsMjJ9s/lXQWvh2zt2DGJpWHeTp+Va6RS7AqgKoGAFHSgLGkumfDjS7aZ5bHWb84yv2iVFA9soRx9Qa4NxZSXEsltbrHEWJRCdxUZ4GT1rpLmwlnt3j+bkcGuSn0/VbOQj7I7r6qMgj8KiSuXF2N3SUhjv453Rgg4I2j+tW74F7p2gkIQnj5azdA1O909n/AOJYVDjG98jH0HetVgzEk8k8kk0oqwTkmZ/2NmmEjyFiOldNp8dwNLDxsMAnClTluecHGKw5EJU/MAccYrmbKae8VhPd3AdRnDRqATg8A4PfA9s1d7GFSooK7O6OoTLOI5RtyCcEYpt/OjafNvPG39e361x2nPP/AGoYBNMyBWBaSIKuQ3YjqMVe16RotFmYyHPAGPrQ9UOLvqSwuHUEVNuxXP6RfmQKrHmt0HNcslZndCV0Ry/NVVo81cYc8VGVNIqxUCc07y89Kl2c1MkfFO4rFMw1DJAeorTZOMVE0dNMTRkm4khbAty59e1d94P8bazbacNOtZzbpDlggiQjk5JJKk5ya5TbGilnqXStYtba+CsGCsNpYdunJrWD1Mqi906zxZr+t6rp8az30j+UxZNsYXDEYz8uMcbhnnrjua4O7sdTtBCLhCBLGJI9yY3KehHqODXoMCJJNDIxLQgM3yHk/I2MY7+lcVOZPJjjDMEVmKqTwOfStUccjLU3SceVn8KezXJPzQ4OPSrqecUKHBXOcYqWOa4tmzGkXT+KNW/mDQSU/MufJAMRC+u01GUn6mMgeu01pv59wDK0KAdSUTaPyHFRxzXEDhojtYd8UxFAJIeeM0PBJtBLLg+hH8q07m91C8Krc3MsoX7odsgVWaB9jMc4B5osBnPBz8xH51X1cwvexpAXYJCis7nl2IyfoATtHsoPer/lZPSuuk8F2+qaRaSHNtdiIHcOQ2eRn/PFIuJ5pjArvPC3xKvNKhSx1eNtR08AgEn99H9CfvDrwfzwMVnz+AdVifC+VIv94P8A41PbfD3UncedJBEueTuyfwxQUeqW7aZ4j0Ro/D99C8e7fJEOHHJwGXgjp6c44qhpvhyO11Rr/ULdFNqC+/HJPT8fxrEXw3FpOjSLpyn7ao3JODiQN3KsORxkcVVtPiBren7IrwRX6AfMsi7XGOMBhx75IPWpbV7E3NRr7UJdWlu7e6uElmb5Y/MJjHYDb0PAFbWvazq+lx2UFn9jmuSm65a4VgO2NoUjB69fasqy8daLcyRz3GkXUMoPzvGgkROuMkYbnntUk2ueEtWupJf7WYSsckvG6D8Ny4pjujds9Zml0W4v57aMPCMKFJAdu3J6DJx3qHTdek1C9itxYRx7hl2D7sHv2GR71BNfeHpdMjsV1m3SNDubEq5Y02w1Dw7pCNeJqSyKziHzMF9hPIzgcA4PJ4+U+hoGrDdT8RX0d9Pb23lqqSFFYJljjjvx+lO8VxXd1BYWqeZLLtLSbeAegBPb1pumar4Yn121srVzNNM6/wCkSArGpzzkvg7uOMDBJAB5qf4lz6volvDdaRd2/wBlkIjkZUDSo2D3OQFPqAMHvyKQ0RfatP8AD/hRYNZnjRpW3tETlmwQQAo69B7VAdcS+sIprcPHbzEqoYYZsevpXk0MNzq2sDzZHmmkPzSSMWJ9yTXqUUEcNtFAvKRABcj0GKYXM3UtVh04IZoJm3527ADnH4+9ZQ8U2Vx+7W3uVLnaCwXGTx/erf1DT7fUbYwzKSOqnoQfWuCbTbnS9VghlMZZnXBX5hgtg9R6ZqW2mJ3ud9dLhx7bh/481cf4sYk2kOQAxdufbA/9mrtb1MSEf7T/APobVxHimVDdwwlcssZYH0ycf+y0yzmJB2wPwqfS9SudIv0u7VsOvDKejD0NRMOc0wn2FMR65pWs2HiK0DwsEuAP3kJPzKf6j3//AFU+a0cMWBO7+8O/1HevIYp5beVZYXaKReQyEgj8a6zTfHl1EFj1GEXC8fvE+V/xHQ/pUOJSkdNIGHDx591/wquwjzzkfUYqS28V6HeBQbowu38MyEY+p5H61Z+1adKSINQs3Pokyk/oalopMrQRF2wiE+/atm2hEMeCcsepqskcpwUkDfR81emK2mnGeVSzqm7A/iPYUikzB1wg3duvdVY/mR/hVdEGBzWRcLevq8b3Jbe+WPoB6fSrnzs2AamRcS/hf7wq1bxbhkDiq9nY5O+TmtEuI1wOKgoxjFxyGNNEfpF+YxXquoeHvBmkL/p7iNtu4RtcuXYZxkIDk/gK5bULzwjGxWw0O5ucNgyS3ckSkY6gbiT9CFrrOYq6fH4YgKveDVLllbdsSKONWGOh+cnrzkEV09l4o8KacgFro08ZVdu/yUZyPQsWJP4mvO5lWaTeI1hUqAY4Xfb9fmZm/WmFBj7gP1p2A9Rf4jaSqN5VhebwPlDqiqT9QxI/Kuf1L4j6rPlbKOK0TIIIG9x6jJ4P5VxLIh6RKfoKY0Cn/lnGB9KVhE17dXF9cvcXMjyyufmd2yT+Jqmd5/gA+pp/2eMc+Un5UhjjX+BB+FAELIx67R+NMEXbK/8AfVWNgPSJB7mpEhH8QUn6UxFMRNu/gA92rmvGN2I4IbNSpZjvbHp0H9a7cLjsK8t8S3hvdcuHzlEOxPoKQEWnXBjlXmu2gfdGreorzuF9jiu106ZprSMIMkjFZVF1Oii9bF43Ch+elWSFZQRyCKz3sOC0smSOcL0rRtoQtrwcjtWLsdNn1K5ABqRTgU1xg5pNrE0CHls96TK0bT0qJlbBHeqQmJcCP5QSMZ6VYs9Os7qRWMeJegbFY+DLO0Z4VSAzd89cD/PerunaqtgjEgPhgsYbJB5wc+3erUWZuaW52ukW8wmexggu3kjAZBbxszAEDJ+Xpyw+m7HWny+CLi4A8uy1SNskktasev1xXWeGLMXHjyC/0qR0sksBLeqX3hZXyDFk/MeQr5Ofu9cYFem4rY5Grs8G/wCFd6uQTDBcndxiSDb/AFp3/Ct/EJHyWg5GDuIH9a92xRii4uRHiC/DvxKLUQfZFx1/1qf41Efhj4hbG62PHo8fH/j9e6YFGKLsXIjxAfC/Wc5ME2fTMQ/9qVdh+GV0wHnWNwTnLBrmMBvyNex4oxRcfIjzC0+H8tqVMOk26EHIZpgxB+pya0R4R1L/AJ423/f4/wDxNd9gUUXHyo4A+D9Sbgw2n/f9v/iacPCGp/3LIfWZv/iK72jAouHKjgj4P1XsNP8A+/7/APxFcNqfgXxLH4lmNrpwuZT+9SQYMIB9GfAyPQ88dDXu2KKT1E4pny3Do2p3OpS2LWs/2lVaWdHjIeNQNzMwxkAfqSAOSAZdI8OXur30kOm20dzNEN/2fcE81RwSCxUcZHGQeeOhx9LajFcT6Zdw2coiunhdYZCcBXIIU/gcVyXgvwG3hq6a/u7lJrpojGI41+RMnJO48k4A7DGT1zxNieTU8U1DTYNH1a5t1ZbkW7FC7oVG4dTg+h9fTmtnwn4HuvFNzOqzww28O3zWfJIDZIKqMA8rg8jrXuUvhnRp9YTVpNPha9QhhIc/eHRtvQsPXGeB6Cr0VjawXc11FAiTzACV1GC+M4z6nnrRygoankl58OgXvW0O5gvLm0ZUmt0jMYB252q7EqX6EjIxnJ6gHqrv4fz3ltJby6rCY5FKsPsXb/v51rt0RI0CRqqqOiqMAU6mlYtRR8//AA28Kya1qN/KJRAbdFAkKbwGJPGMjsD+Vej/APCA3f8A0GYv/AL/AOzqX4beHr3QNGu11Kz+zXk1xuPzqxZAq45UkdS9dpVBY4YeALrPza0mPazA/wDZ6zdS+Gccc0+rSao8jW9szLGsAXcygnk56Y4xj8a9Lqtfp5mn3Sf3omH6GluFjyHU1CXDAnHLH82J/rXA67Gt5q+YmVtkKqSD7k/1rsPGJ2vJjsrfyry/Td0dqCDjJJp2Almt2jOGFVmSrUjs3UmoSKBEIjzTxbORkKacCV6HFSC5kUdaBoqvGyHBFQnircsrP1NVyvNAEeK6Xwlqd1FrkTSSySxpFKRG7kqD5bYOPUdR9K57ZmtzwrLb2uuRyXYPkBGD4wTgjaSB3xnOPb2pMEenagVurBpJLXaY4YyWwcg4A3DjoSGNYlrbqDubp2rt0sNPl067uNOSK7Q2rqrxhSWbPB45OBgfh9a8yn1v963lqRjjBrGa1N6b0OkadVXC8YqjNcE9DWC2sysOgqFtTlPWs2i7m8m7ooUZOePWtDT9H1DVJNlnbPMQcEqOFOCeT0HTuRWhpGp+G7BVafTLi8m6kzyDaOMEBQMEdThs9etdQnxJsYolji011RQAqhwAAOw4rrMClZfDe9mi3Xd5DbMcEKqGQ++eQAfoTV0fDKHHz6kW/wC2H/2VMb4nxqMjTB+Nxj/2Wuf8TePb/WdOFrpxl0x94LTRXGSw9Pugjt0PqCDngEWNb0Lw14ecR6p4kSGU/wDLJYC78jIyqkkfU8VliXwESB/wk8zseipYyk/+g15NqkpgvXjac3JXIY7cYb9a67wZHDNb6msTgj+zjI+BgllkjOOR0wT6dKLAa2q20dvdutus8cQO3bcptkBwCdw7delZ4TBzkZ9cVs65nZZSsrq1xbwz4dyzfNDGTkk5PzbqxQzvJ5cQ8yT+6q5P4+n40IRIFPr+lKD8+xcvIediDJ/+sPc11WifD3WNTCyXzCxgPUYO8/h1/l7GvSNF8J6Roaj7LbAyg582Tls+vsffr6k0AeSXfhrU7Twzfa1fK1pbQQlwpBLsTwowBnqRzwMHOa8GmH7xj719cfF0H/hWeqsGYbfK6HAOZFGD6jn+VfJEv3jSAh6V1nh+XzbLYTjL7M59s1yZra0GXcs9sGxKcSRj1I6j64/lUzV0a0XaaNI6jMz+QrENu/1bcA11cLxC1AiJ244rlbqcT7WjjXzz94Fec1OdZgh3IZVBXqAe9YtX2Oly5dzf2oyjn9alRFIAArlINXMr/IWck8KozW7aXreT88M4YfwmMg/rQ4tCjUTNERjvVS+ure1jJYjcentVjyZpHhW4lhtI5QGV2k8zAIz8wj3Mv0IzVY2dok8q3V3kD/VvbwmQMfcOUIFCQ3Lsc4LmSW5na3VmaTawGO44J/lVpoPtcvlR5TABbA6t/n+VNvfNhullWcYQEIETaWyO4ycfrWjodpJ5v2iYlVILc/zrZOyOZq7sfTXhoWz+HrC5tLKKzjuYI5zDGBgFlB5I6nGOevFa1YXhS/hudGS0jJMmnbbKfg4EqIu9Qe4BOMjjit2qMgooooAKKKKACiiigAooooAKKKKACiiigAooooAKKM0ZoAKKMijIoAKKMikzQAtMlTzInTONykZp+aM0AeLeOdF1KKWVfscz+YrLEY1LB2IOACO5x06+1eWQ2z28CxSoUkXIZWGCDnkGvrs18va/creaxe3qLtW4nkmA9NzFv600JmWbb5N29cVA0Q/vrTXlIyM1CZfegCVrdsZAzUDIQeRTxOwGA1JndQBFtJNOCZqZIs1ZS2J7UCKaxH0q8NGv7o20+moHYMAGjl2vE+QADnGCTggj1POVOJ47XOKvW0TQOHjJVhxke/UfSkxp2I9N1PUtOdnt7h7ecHbIqfJyDz8vTI9sY9Kp3Ej3Uryli0pOW6ZNX76xmum82Hy45T97C7Vbv0HAOfTA9qr/ANj3bQGW4ilXYQDJEN4Gc46cjoazaNotGf5xGQetMe5xxTJJImlMQfLjo2MbqryAg1DRZ1gvosfeb/vhv8KadUj6fvAPXy2P9KrZpCM10XMSx/acGc/vSfUxt/hTG1WL/pp/3warlaTaPSi4ilcf2fNMztbS73OSRuGTV3R76PSZpWtoJSsttPAwHX54yoPPoSD+FRslOiG1s0AdTZ3en6ra6eNf137Obe2EG2C0mbIDNjJVfQjpj6mu80TxJ8OtCQCzuX80f8tXsZy3fp+7wOpGR175ryEn5cUgY0CPex8S/CZ5/tOT/wAA5/8A4ipB8R/CzdNRf/wFl/8Aia8CDmpVdhSCx6f8SvGGh6t4A1Sysb4yXEixlUMLrkCRSeSB2BNfMc33jXo9/mW0dcdVP4153dIElODkUIGVjSq7I4ZWKsDkEHBFIabTA1RrV9JF5bzZHQnABI+tNV0b7wHNZ4NShvlqbIrmb3L6lMjaAOa29K1NYlMbHA7VzUb8E+lSCXaetJq44ys7neC7VwCCDmiZY5o9r/mDXL6fqHlocsTj1q0dS3n71Zcup0KaaNeGztYW8xsuR03GrlzrjaQkV1alRPGyuhIyAQcjjv8ASuWk1QjjNULq8e6lUk/IvAFaRi+plKaS0PSfh78QZvD1xMZoWu1n4ZWlK/MTnI4POc5POeOmK9YX4jMwB/slRkf8/X/2FeBeFdOM0/2pwTHGfl9Ca7pZW9RVmDPRf+Fhnvpa/wDgT/8AYUv/AAsP/qGD/wACP/sa8781vWjzG9aBHoR+ITHppyj6zE/+y0w/ECc/dsoh9XJrgPNb1pwkb1/SgDu/+E9vD0tbcf8AfX+NIPHWoH/lhaf98t/8VXDq7f3jUqs/94/pQK52h8bamT8sdmB7xsf/AGalHjPVCQNllkn/AJ5P/wDF1yKs2PvGmyMflySef6UAdr/wmGpDqlmf+2bf/F0h8ZagP+Wdp/37b/4quL3UbqYzsv8AhM9R/wCedr+CN/8AFUw+MtS/u24+kZ/xrkN1JupAdf8A8JjqWOsP/fH/ANekPjDUj/y0iH/bMVyO8+tG8+tAzq28W6oek6j6Rr/hTf8AhK9W/wCfof8AftP8K5befWjefU0AdOfFWr44vSP+2Sf/ABNRnxRrPbUXH/bKP/4muc3n1pN59TQBvy+JddZSE1eZD6iGE/zSoh4j1/r/AG7cn/thB/8AG6xN59T+dNL7ecnHfmgZvf8ACTa8P+YzOf8AtjD/APG6Y3inXh/zGJv+/EP/AMRWGzH1P51C7H1P50h2Ni58ZeIYIXk/tiXCKWP7iHt/wCvK7m5QIEXooAFdRqzkaXd5b/li46+1efSTk96aEyWWYEnFQGX3qBpCe9R76ZJfRwT1q5GFOKxlk561aiuCO9AzdhRauRIKxoLvBGa1Le7jPU0gNCOIVcSIYqnDOrdDV1JBgYNSOxMkYqaLfDIssTskinKspwQfUGo0YVKGBpiI5lguSovLK2udowDJHhvqXXDH8TWTq+jQXEfm2UAhdR8yBiQ30z/jWxxk05RkVLSZak0c1QRS0GqAYabT/apEt2bluBTQivtLcAEmpUtX6kge1WlRUGFFI8qR9T+ApgR/Z2/vCoXUIcBgT7Urzs/A4HoKjoAcCakDYqMU4GkBIw3oVIrhNYsXt7txg4JyDXcg1UvLEXY5xQM89ZcHFMxitu90tre6cnmIfyrPktGVhnGCMg57UyWipmlDYp7RMpORxSCNj0FAB5ho3n1pywEnninRxZbBFACJIwqUTNjg0piz0HfsKkS26Bic+w60BcYCznNXrHT5rqdYkXJJ59hV/TtFkuER8Hbn6AV1FlYxWMflxfeP3m70mwNKyhS0tY4IxhUGBVsP71jzWbu++KTA2+vX/wDXVV47iLht+B3zxSuFjo9/vRvOetcz5kn99vzp6SSFsB2Y/wC9+tK4KNzommWNCzsFUdSTTo5t6K6nKsMg1g3j21pCGuHLzHhI8k8n1P8ASqUF+080cMcj/MezHgD/ACfyp3BxOvVznrUqufU1n2pJjye54q0DQSW1c46n86Xdlxnpg1AGpQ/zkegpgWRijcKhDUu6gCRnwV+tLuFV3fC59x/Ol3Y70hmT4l1ubRoreSFFfzGYEN7Yrnm8aakqCQ2YCdQxBwRVnx4+bWzxz87fyFc1Nr13PbNBJDAYynlj5D8o56c8dR/3yvpTMqjmrcqOlbxXqyvcI1lBvtyRIgkG4YznAzlgMEkjIAGTTLnxbqtteSWkthGJ04ZFbdjjPappr8y22o20nlTCRpDH5t+uxxuJUDDblyCBgYHy9Rk1j6h4gubHxFcTWn2eVA+VLRK2cqOdw5/WpUmzkpYitNtJL+rHQeH/ABHPrN7LBLEiIsRb5fXIFdENjDlRn6VwnguZ59bvJpfvyRFmIGMksM8V3BOGz2PBqj0Fe2o4pH/dH5Unlx/3F/KjdSFqQxvlxqcbF9uKayp/dH5U5jkVEW7HrQMyvEO4aJdGNSTgcAZ43DP6Zrzxnr1RjXN6v4bgu90trthm7r/C3+FCdgaOLLU3NSXVtPaTGKeMo47Hv9PWoM1RI8NipVfFV80oagC8kuKsx3JGOayw9SLJQBuw3jL0atCHU2HU5rmFl96nS4I70AdhFqikDNXYr6NujCuKS5x3qzHeEDrSsFzs1nBJ54qeOQY61yEV+6/xVeh1RgBnmpsVcKkWFn5PAqVI1T3PrTiaoqwixonQc+tIzgDJOB71G8wXgcmqzsWOWNMRLJcZ4T86gJJOScmikpXAWj60maSkArPtUkDOOwqhZPdG8md4QkchzyeRV6lAwcmgCZTTs1EDTgaYDJreKYfMo9/esm+0ZZExEMc447Ctqk60gOZn0adYkWMbv73Gfbms11Fu3lPHkjk9ufWu42jFQz2Nvcj97Ere+KdwOOBjYk8HJwAoq59l3qnADKOgHJrpIdMtYj+7iC/SrkdtEnSNRj2oA5uGwnucfuVVick4x+QrXs9Cii+eY7mPX3/GtVRjpxTjwM0CuISkMYCqAOwFRoxYHvn9ay7nU5PNZVsrhwOjDGD79aoSSXc4AMN+AOgBQY/SkI6RtRtrdR5lwoOcDHP48VnzeJ441xDbPKc87iFHHT1z+lYwsZ2PFre/jIn+FSDTbkkf6Ldc8czL/wDE0WAjm1u5d8rbxRjGAFBP49aj/t26jwLdPJI/jI3Grn9kXfa0l/Gdf/iad/Y92R/x6N+M6/8AxFGg7mQsk9xIHkkaSQkAFiT8x6fkK2tGXHm3K9BiOI4/AH+v400aNdjkWfPr54z/AOg1oWNrcRywwvbxxRJl/lk3HPQZ4HqfyoEdBAAkSqOgGKnBquhwKkBoEThqFb5mP4f5/Oot1CHlj6n+lAizupd1QBqN1AySSTEbH0GaC4qEnII9aYr7kVvUZoAr38qDYXjVsNnmq4voe8C/kKW+UuKz/KOaAZfN7CHyIFwR0wKab+DOPIT8h/hVMxnGT2pphJOaQkatpdRvN8kSqcdRWh5gIwelYlkhSTNagNMpE6yZBB6il3VWdthD+nB+lP3UDJS1ROe46ikLUxmpDBmyM1Exoc7fmHTvTC2aQyte2VvfwmK4jDDse6/Q1xuq6BPYFpYszW4/iA5X6j+tdwaaTTTsDR5hS5rrNX8OJMGnsgEl6mPoG+noa5SSN4ZCkilXXqD2qr3JasANKGpmaM0xE6vT1eq4NOBoAtrJUizY4zVINTg9IDRScjvUy3OO9ZgfiniSgDu3cKOTUDyM/HQU3nqeT65pOfQfnQaMCKaRTufQfnTDu9B+dIkQ0hpTn2ppz6UDsJRSZPpRk+lADulFNyfSjcfSgCQUoNR7j/d/WnBj6frTESU6owT6frTgT6D86QDqcKZlvQfnTxv/ALq/nTAkUYqRaiBk/ur+f/1qcDJ/dX/vr/61AiYUH5sr+dR7pMfdXP8Avf8A1qAZR/An/fX/ANagCUAUoAqPMv8AcT/vr/61KGl/uJ/31/8AWoES0qcnPpwKizN2VP8Avo/4UoMoUDYn/fX/ANakBPmjNQ7pf7if99H/AApd0n91f++v/rUwJs0xPmZm9Tgdun/1803dJ6L+dClwAPlpCLANKG96g3P/ALNKC47igZY3URMdpz6n+dV9z+opkTzNGrblwRnp/wDXoAvbqM1VDS/3l/L/AOvRul7lfyoCxZ3VHG37sD0JH5VAWlHI201WlUEbVIzmgCWT5qhwM9BQzSn+FR+NN/eeifnSAfgelMTpj04qRYyUBM0YPoQ3+FNMRBys0Rz1+9z+lAWHx8Gpw3FQonrcRD8G/wAKc52Y2SJJnrjIx+YoGiTdxTEfB2f3en0qLzHP8K/nTGZyQwABFAy0WphNRb5Mfw00u54+WgZLuzUJIRsdj0pCX7baaSzDB280AOJyKTNRAyZKllBHt1FLh/7w/KgY/NZ2p6VBqEfzjbIPuuByP8RV4+Z6r+VJ8+Oo/KgRwF7YT2E3lzL/ALrDow9qq16Fc2iXcLQzKrIfbofUVx+p6RNpz55eFj8r4/Q+9UmS0ZwpQaSigQ7NKDTM0uaAJAxpdxqPdRu4oA9BpMU7BpCKRoNNNNOppoENpppxpvNAIaaSlP0pOfSgApaOaOaAFApwFNBIHT9adz6CgGOFOFMyfQfnTl3dcD86BEiinimDd6D86eC3oPzpgPAp1MG70H508E+g/OgBwpwpoJ9P1pwz6UCFpRSc+lLk+lAC0tJk+lGT6UhC0UmT/doyf7ppALRTcn+6aNx/un86YDs0ZpuT/dP51KkRcAllX6mkARBWcb22r3PoKVlSNVVM4Aq3BpyzI+64VF7svzVXZ4hwYyzY6lv/AK1ADAaKazsfuxxgfU0zfKOiJ/31/wDWoGTUVAZJv7if99H/AApPMn/uJ/30f8KAJiKYRUZkn/uJ/wB9H/Ck3z/880/76/8ArUAS96Mc1D5k/wDzzT/vr/61HmT/APPJP++6AJgKdtFV/Nn/AOeS/wDfX/1qPNn/AOeS/wDff/1qBljFG0VB5s3/ADyX/vv/AOtR5sv/ADyX/vv/AOtQMl2ikKio/Ml/55r/AN9//WpDJL/cX/vr/wCtQA8imkU3fJ/cX/vr/wCtSFn/ALq/nQMHG4ZHBHQ0ituHv3FGX9vzpjbw2/A9xnrSAlopgLEdqX589qAFpkkSSxtHIoZGGCD3p3ze1J83qPyoA5DWNEexJmhBa3P5p9fb3rGxXo7KWUq20qRggjOa5TWNFa1JuIBmE/eUfwf/AFqpMlow8UYp2KXFUSR4op5FJjFIZ6OVphFTEVG1I1aIiKjNSmoyKCRpFIacRxTTQIaRRinYpMUgEoApaUDNAABS0YxSgZPsKAADJzUgFIBTxTAUDmnAUgFPFACgU4Ug5p4FAgFKKUClFABS0YpcUAGKCKB1o6mkACijPGaWgQmKSlpCaADIFAammloAsremC0khCkiQgHn3H+FVyxZsmo5fuj/eFKKQD80UzvmlzQMdSUmaSgB1JSZpKBjqTikzSbuKAH5FGaaDmigB1FNzRmgBaKTNFAwpMU7FJ2oGJim4p+KSgCPG047HpT6UqGGDSJzweo4oAMUmKdijFADcU1lDAgjIPY0+k60AcnrOjfZmNxbjMJPzL/c/+tWNtr0MqGBBGQeCD3rlNZ01LKdWiP7uTOF/u1SZLRj7aNtSlaAtMR//2Q==";


        @SuppressLint({"NewApi", "LocalSuppress"})
        byte[] bis = Base64.getDecoder().decode(data);//Base64.decode(data, Base64.URL_SAFE);
        Matrix matrix = new Matrix();
        matrix.postRotate(180); /*翻转180度*/ //后置摄像头是90°  前置摄像头是270°

        Bitmap bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        Log.e("byte-------------", bis.length + "" + bis.toString());


        if (bitmap != null)
            iv_byte_image.setImageBitmap(bitmap);
    }

    private void read() {

//        String hexString = et_send_data.getText().toString().trim();
//        byte[] data = HexDump.hexStringToByteArray(hexString);
//        receive(data);
        if (!successPort) {
            Toast.makeText(SendActivityTypeOne.this, "没有连接端口", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            byte[] buffer = new byte[8192];
            int len = userPortCommuManager.mPort.read(buffer, 200);
            receive(Arrays.copyOf(buffer, len));
        } catch (IOException e) {
            e.printStackTrace();
            status("connection lost: " + e.getMessage());
            tv_receive_data.setText("连接失败啦啦啦啦啦啦啦");
        }
    }

    /**
     * 将16进制字符串转为String类型字符串
     *
     * @param hexStr
     * @return
     */
    public static String hexStringToString(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    private void receive(byte[] data) {
        SpannableStringBuilder spn = new SpannableStringBuilder();
        spn.append("receive" + data.length + "   bytes\n");
        if (data.length > 0)
            spn.append(HexDump.dumpHexString(data) + "\n");
        tv_receive_data.append(spn);
        Log.e(TAG, "spn数据: " + spn);
    }


    //数据接收回调
    @Override
    public void onDataReceive(String content) {
        tv_receive_data.setText(content);
        Log.e(TAG, "onDataReceive: " + content);
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "onError: " + e.getMessage());
    }

    //接收端的两个监听回调
    @Override
    public void onNewData(byte[] data) {
        if (data != null) {
            Log.d(TAG, "byte------" + String.valueOf(data));
            mainLooper.post(new Runnable() {
                @Override
                public void run() {
                    receive(data);
                }
            });

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    receive(data);
//                }
//            });
        }
    }

    @Override
    public void onRunError(Exception e) {
        mainLooper.post(new Runnable() {
            @Override
            public void run() {
                status("connection lost: " + e.getMessage());
                if (successPort) {
                    if (userPortCommuManager != null) {
                        userPortCommuManager.disconnect();
                    }
                }
            }
        });
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.purple_700)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_receive_data.append(spn);
    }

    private Thread receiveThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (successPort) {
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        }
    });
}