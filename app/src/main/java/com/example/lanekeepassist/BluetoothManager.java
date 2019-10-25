package com.example.lanekeepassist;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BluetoothManager extends Thread {
    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;

    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] mmBuffer; // mmBuffer store for the stream

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; // handler that gets info from Bluetooth service

    private UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private static final String deviceMAC = "DC:A6:32:3C:18:BE";


    BluetoothManager(){}

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    boolean initializeAdapter() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.isEnabled();
    }

    void initializeSocket(){
        BluetoothSocket tmp = null;
        device = adapter.getRemoteDevice(deviceMAC);
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        }catch(IOException e){
            Log.e(TAG, "Socket's create() method failed", e);
        }
        socket = tmp;
    }

    public void connectSocket(){
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

    void initializeStreams(){
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    public void write(byte[] bytes){
        try {
            outputStream.write(bytes);

//            // Share the sent message with the UI activity.
//            Message writtenMsg = handler.obtainMessage(
//                    MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
//            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            handler.sendMessage(writeErrorMsg);
        }
    }
}
