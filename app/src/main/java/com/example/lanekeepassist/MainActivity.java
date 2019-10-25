package com.example.lanekeepassist;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothManager bluetoothManager = new BluetoothManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //dialog to turn on bt if disabled
        if(!bluetoothManager.initializeAdapter()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void connectToPi(View view){
        bluetoothManager.initializeSocket();
        bluetoothManager.connectSocket();
        bluetoothManager.initializeStreams();
    }

    public void sendData(View view){
        byte[] byteArr = {'B'};
        bluetoothManager.write(byteArr);
    }
}



