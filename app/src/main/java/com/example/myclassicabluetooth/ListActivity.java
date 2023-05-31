package com.example.myclassicabluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.Set;

public class ListActivity extends AppCompatActivity {
    //选择连接设备地址
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public final int REQUEST_CODE_SEARCH_BLUETOOTH_DEVICES =11;
    private ArrayAdapter<String> listAdapter;
    int RESULT_OK = 1;
    private BluetoothAdapter mAdapter;

    /**
     * 使用广播寻找其他设备
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //获取蓝牙设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //传入打包数据
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    listAdapter.add(device.getName() + "\n" + device.getAddress());
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (listAdapter == null) {
                        Toast.makeText(ListActivity.this, "木得设备", Toast.LENGTH_SHORT);

                    }
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    Log.e("mReceiver", "ACTION_ACL_DISCONNECTED");
                    Toast.makeText(ListActivity.this, "ACTION_ACL_DISCONNECTED", Toast.LENGTH_SHORT).show();
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                    Log.e("mReceiver", "ACTION_ACL_DISCONNECT_REQUESTED");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    Log.e("mReceiver", "ACTION_ACL_DISCONNECTED");
                    Toast.makeText(ListActivity.this, "ACTION_ACL_DISCONNECTED", Toast.LENGTH_SHORT).show();
                } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                    Log.e("mReceiver", "ACTION_CONNECTION_STATE_CHANGED");
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    Log.e("mReceiver", "ACTION_STATE_CHANGED");
                } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    Log.e("mReceiver", "ACTION_BOND_STATE_CHANGED");
                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    Log.e("mReceiver", "STATE_OFF");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        listAdapter = new ArrayAdapter<>(this, R.layout.device_name);



        //注册寻找的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction("ACTION_ACL_DISCONNECTED");
        this.registerReceiver(mReceiver, filter);
        //搜索结束的广播
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        this.registerReceiver(mReceiver, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));

        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);



        ListView lv_device = findViewById(R.id.listView);
        lv_device.setAdapter(listAdapter);
        //选择连接设备并传输数据给intent
        lv_device.setOnItemClickListener((adapterView, v, i, l) -> {
            String info = ((TextView) v).getText().toString();
            if (info.equals("没有已配对设备")) {
                Toast.makeText(getApplicationContext(), "没有已配对设备", Toast.LENGTH_LONG).show();
            } else {
                String address = info.substring(info.length() - 17);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                Log.d("点击蓝牙", "点击成功");
                Log.d("点击蓝牙", address);
                setResult(REQUEST_CODE_SEARCH_BLUETOOTH_DEVICES, intent);
                Log.d("点击蓝牙", "选择成功");
                finish();
            }
        });
        PrintDevice();

    }



    //打印已配对设备
    @SuppressLint("MissingPermission")
    public void PrintDevice() {
        //打印出已配对的设备
        @SuppressLint("MissingPermission")
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                listAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            listAdapter.add("没有已配对设备");
        }
    }

    //销毁
    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }
}