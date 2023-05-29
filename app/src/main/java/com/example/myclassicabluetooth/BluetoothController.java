package com.example.myclassicabluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

//一个蓝牙控制类
public class BluetoothController {
    private BluetoothAdapter mAdapter ;

    public BluetoothController() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    //判断是否支持蓝牙
    public boolean isSupportBluetooth(){
        if(mAdapter != null){
            return true;
        }
        return false;
    }
    //判断当前蓝牙状态
    public boolean getBluetoothStatus(){
        assert(mAdapter != null);
        return mAdapter.isEnabled();
    }

    //系统打开蓝牙
     @SuppressLint("MissingPermission")
     public void turnOnBluetooth(Activity activity , int requestCode){
        activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), requestCode);
     }

}
