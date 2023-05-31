package com.example.myclassicabluetooth;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    Button menu;
    BluetoothController bluetoothController;
    public  String TAG = "mainActivity";


    int REQUEST_CODE_SEARCH_BLUETOOTH_DEVICES = 11;
    int REQUEST_CODE_TURN_ON_BLUETOOTH_ADAPTER = 10;

    public static BluetoothSocket mSocket;
    public static InputStream mInputStreamForAdapter;
    public static OutputStream mOutputStreamForAdapter;
    public static final UUID MY_UUID = UUID.fromString
            ("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothDevice device;
    BluetoothAdapter mAdapter;




    //传入回调信息
    //TODO:为什么没有运行
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: 运行了");
        if(requestCode ==REQUEST_CODE_SEARCH_BLUETOOTH_DEVICES && requestCode == Activity.RESULT_OK) {

                String address = data.getExtras().getString(ListActivity.EXTRA_DEVICE_ADDRESS);
                mAdapter = BluetoothAdapter.getDefaultAdapter();
                device = mAdapter.getRemoteDevice(address);
                Log.d(TAG, address);
                ConnectThread(device);


        }
    }

    @SuppressLint("MissingPermission")
    public void ConnectThread(BluetoothDevice device) {
        //用服务号得到socket

        mAdapter.cancelDiscovery();
        try {
            mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e) {
            Toast.makeText(this, "连接失败1", Toast.LENGTH_SHORT).show();
        }
        //连接socket
        try {
            mSocket.connect();
            Log.e("蓝牙未连接成功", "为什么？");
            Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
            mInputStreamForAdapter = mSocket.getInputStream();
            //CONNECT_STATUS = true;
            ReceiveData receive_thread = new ReceiveData();// 连接成功后开启接收数据服务
            receive_thread.start();
        } catch (Exception e) {
            try {
                Toast.makeText(this, "连接失败2", Toast.LENGTH_SHORT).show();
                mSocket.close();
                mSocket = null;
            } catch (Exception ee) {
                Toast.makeText(this, "连接失败3", Toast.LENGTH_SHORT).show();
            }

            return;
        }

    }

    //发送蓝牙数据
//    static void write(Context context, String word){
//        if (mSocket != null){
//            try {
//                mOutputStreamForAdapter = mSocket.getOutputStream();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        if (null != mOutputStreamForAdapter){
//            try {
//                mOutputStreamForAdapter.write(word.getBytes());
//                Toast toast = Toast.makeText(context, word, Toast.LENGTH_SHORT);
//                toast.show();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }else {
//            Log.e("SendCommand", "mOutputStreamForAdapter = null");
//        }
//    }


    //接收返回的蓝牙数据
    static String readStr;

    static boolean startReceive = true;
    void receive(){
        byte[] byteArray = new byte[1024];
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (startReceive){
                    if (null != mInputStreamForAdapter){
                        try {
                            if (mInputStreamForAdapter.available() <= 0){
                                continue;
                            }else {
                                //必须睡眠一定时间，否则出现接收数据丢失第一位
                                Thread.sleep(100);
                            }
                            int byteLength = mInputStreamForAdapter.read(byteArray);
                            readStr = new String(byteArray, 0, byteLength);
                            MainActivity.readStr = readStr;
                            Log.e("mainactivity", MainActivity.readStr);
                            Log.e(TAG, "readStr: "+readStr);
                            if ( readStr.charAt(0) == '1'){
                                Log.e(TAG, "get it");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }).start();
    }


    //自定义的toast 方法
    public void toastShorter(final String word, int delay_ms) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(MainActivity.this, word, Toast.LENGTH_LONG);
                toast.show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        toast.cancel();
                    }
                }, delay_ms);
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 //  startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1);
        menu = findViewById(R.id.menu_button);
        bluetoothController = new BluetoothController();
        initPermission();
        initView();

        receive();



    }

    public void initView(){
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu =
                        PopupMenuOnView.popUpAMenuOnView(MainActivity.this, menu, R.menu.lb_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        //请求打开蓝牙
                        if(id == R.id.start_bluetooth){
                            if(bluetoothController.isSupportBluetooth()){
                                if(bluetoothController.getBluetoothStatus())
                                {
                                    toastShorter("蓝牙已打开", 600);
                                }
                            else {
                                toastShorter("蓝牙未打开", 600);
                                bluetoothController.turnOnBluetooth
                                        (MainActivity.this, REQUEST_CODE_TURN_ON_BLUETOOTH_ADAPTER);
                            }


                        }else {
                                toastShorter("不支持蓝牙", 300);
                            }}
                        else if (id == R.id.find_bluetooth) {
                            if(bluetoothController.isSupportBluetooth()){
                                if(bluetoothController.getBluetoothStatus()){
                                    Intent serverIntent = new Intent(MainActivity.this, ListActivity.class);
                                    startActivityForResult
                                            (serverIntent, REQUEST_CODE_SEARCH_BLUETOOTH_DEVICES);
                                } else {
                                    toastShorter("正在打开蓝牙", 300);
                                    bluetoothController.turnOnBluetooth
                                            (MainActivity.this, REQUEST_CODE_TURN_ON_BLUETOOTH_ADAPTER);
                                }

                            }else {
                                toastShorter("抱歉，无蓝牙", 300);
                            }

                        }
                        else if (id == R.id.disconnect_bluetooth) {
                            //断开蓝牙连接
                            if (null == mSocket){
                                toastShorter("还未连接设备", 300);
                            }else {
                                try {
                                    toastShorter("关闭设备成功", 400);
                                    startReceive = false;
                                    mSocket.close();
                                    mSocket = null;
                                    if (null != mOutputStreamForAdapter){
                                        mOutputStreamForAdapter = null;
                                    }

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        return false;
                    }
                });
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        DimBackgroundClass.dimBackground(MainActivity.this, 0.5f, 1.f);
                    }
                });
            }
        });
    }

    //动态申请权限
    void initPermission() {
        List<String> mPermissionList = new ArrayList<>();
        mPermissionList.add(Manifest.permission.BLUETOOTH);
        mPermissionList.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mPermissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            mPermissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
            mPermissionList.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        mPermissionList.add(Manifest.permission.BLUETOOTH_PRIVILEGED);
        mPermissionList.add(Manifest.permission.WRITE_SETTINGS);
        mPermissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
        mPermissionList.add(Manifest.permission.INTERNET);
        mPermissionList.add(Manifest.permission.CHANGE_NETWORK_STATE);
        mPermissionList.add(Manifest.permission.CHANGE_WIFI_STATE);
        ActivityCompat.requestPermissions
                (this, mPermissionList.toArray(new String[0]), 111);
    }


    //蓝牙接收线程
    public class ReceiveData extends Thread{

    }


}
