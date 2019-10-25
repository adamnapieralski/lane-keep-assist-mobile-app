package com.example.lanekeepassist;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class ClientConnection extends Thread {
    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");


    public ClientConnection(){
        BluetoothSocket tmp = null;
        adapter = BluetoothAdapter.getDefaultAdapter();
        device = adapter.getRemoteDevice("DC:A6:32:3C:18:BE");
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        }catch(IOException e){
            Log.e(TAG, "Socket's create() method failed", e);
        }
        socket = tmp;
    }

    public void run(){
        try{
            socket.connect();
        }catch(IOException connectException){
            try {
                socket.close();
            } catch (IOException closeException){
                Log.e(TAG, "Could not close the connection");
            }
            return;
        }
    }
}
