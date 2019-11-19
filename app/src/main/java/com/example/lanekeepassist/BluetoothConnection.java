package com.example.lanekeepassist;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Handler;

public class BluetoothConnection extends Thread {

    // String for MAC address
    private static final String deviceMAC = "DC:A6:32:3C:18:BE";
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    private BluetoothAdapter btAdapter = null;
    private BluetoothDevice btDevice = null;
    private BluetoothSocket btSocket = null;

    private Activity mMainActivity;
    private MainActivity.BluetoothHandler mHandler;

    private InputStream mmInStream = null;
    private OutputStream mmOutStream = null;

    public BluetoothConnection() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btDevice = btAdapter.getRemoteDevice(deviceMAC);
    }

    public void connect() {
        try {
            btSocket = createBluetoothSocket(btDevice);
        } catch (IOException e) {
            Toast.makeText(mMainActivity, "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) { }
        }

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            //Create I/O streams for connection
            tmpIn = btSocket.getInputStream();
            tmpOut = btSocket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];
        int bytes;
        // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    Message msg = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("key", readMessage);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                } catch (IOException e) {
                    break;
                }
            }
    }

    //write method
    public void write(String input) {
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
        try {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
        } catch (IOException e) {
            //if you cannot write, close the application
            Toast.makeText(mMainActivity, "Connection Failure", Toast.LENGTH_LONG).show();
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    public void checkState() {
        if(btAdapter==null) {
            Toast.makeText(mMainActivity, "Device does not support bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mMainActivity.startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public void setHandler(MainActivity.BluetoothHandler handler) {
        mHandler = handler;
    }

    public void setmMainActivity(Activity activity) {
        mMainActivity = activity;
    }

    public void close() {
        try {
            btSocket.close();
        } catch (IOException e) { }
    }
}
