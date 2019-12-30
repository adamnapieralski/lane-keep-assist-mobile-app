package com.example.lanekeepassist;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnection extends Thread {

    // String for MAC address
    private static final String deviceMAC = "DC:A6:32:3C:18:BE";
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;
    private BluetoothSocket btSocket = null;

    private Activity mMainActivity;
    private BluetoothHandler mHandler;

    private InputStream mmInStream = null;
    private OutputStream mmOutStream = null;

    static final int STATE_ASSISTANT_RECEIVING = 1;
    static final int STATE_IMAGE_RECEIVING = 2;
    static final int STATE_SETTINGS_RECEIVING = 3;

    private int mState;


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
            Toast.makeText(mMainActivity, "Connection failed", Toast.LENGTH_LONG).show();
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
        } catch (IOException e) {
            Toast.makeText(mMainActivity, "Streams creation failed", Toast.LENGTH_LONG).show();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;



        mState = STATE_ASSISTANT_RECEIVING;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[512];
        int bytes;

        byte[] imgBuffer = null;
        int numberOfBytes = 0;
        int index=0;
        boolean imgFlag = true;

        // Keep looping to listen for received messages
        while (true) {
            switch (mState) {
                case STATE_ASSISTANT_RECEIVING:
                    try {
                        bytes = mmInStream.read(buffer);            //read bytes from input buffer
                        String readMessage = new String(buffer, 0, bytes);
                        Message msg = mHandler.obtainMessage(BluetoothHandler.STATE_ASSISTANT_RECEIVED);
                        Bundle bundle = new Bundle();
                        bundle.putString("assistant", readMessage);
                        msg.setData(bundle);

                        mHandler.sendMessage(msg);

                    } catch (IOException e) {
                        break;
                    }
                    break;

                case STATE_IMAGE_RECEIVING:
                    if (imgFlag) {
                        try {
                            byte[] temp = new byte[mmInStream.available()];
//                            bytes = mmInStream.read(buffer);
//                            String readMessage = new String(buffer, 0, bytes);
                            if(mmInStream.read(temp)>0)
                            {
                                String str = new String(temp, "UTF-8");
                                Log.i("BT_CONNECTION", str);
                                try {
                                    numberOfBytes=Integer.parseInt(str);
                                } catch (NumberFormatException e) {
                                    numberOfBytes = 0;
                                }
    //                            numberOfBytes=Integer.parseInt(readMessage);
    //                            Log.d("BTconnection", Integer.toString(numberOfBytes));

                                imgBuffer=new byte[numberOfBytes];
                                imgFlag=false;
                                break;
                            }

                        } catch (IOException e) {
                            break;
                        }
                    }
                    else {
                        try {
                            byte[] data=new byte[mmInStream.available()];
                            int numbers=mmInStream.read(data);
                            Log.d("BTconnection", Integer.toString(numbers));

                            System.arraycopy(data,0,imgBuffer,index,numbers);
                            index=index+numbers;

                            if(index == numberOfBytes)
                            {
                                mHandler.obtainMessage(BluetoothHandler.STATE_IMAGE_RECEIVED,numberOfBytes,-1,imgBuffer).sendToTarget();
                                imgFlag = true;
                                index = 0;
                                mState = STATE_SETTINGS_RECEIVING;
                            }
                        } catch (IOException e) {
                            break;
                        }
                    }
                    break;

                case STATE_SETTINGS_RECEIVING:
                    try {
                        Log.d("BTconnection", "STATE_SETTINGS_RECEIVING");
                        bytes = mmInStream.read(buffer);            //read bytes from input buffer
                        String readMessage = new String(buffer, 0, bytes);
                        Message msg = mHandler.obtainMessage(BluetoothHandler.STATE_SETTINGS_RECEIVED);
                        Bundle bundle = new Bundle();
                        bundle.putString("settings", readMessage);
                        msg.setData(bundle);

                        mHandler.sendMessage(msg);
                        mState = STATE_ASSISTANT_RECEIVING;

                    } catch (IOException e) {
                        break;
                    }
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

    public void setHandler(BluetoothHandler handler) {
        mHandler = handler;
    }

    public void setMainActivity(Activity activity) {
        mMainActivity = activity;
    }

    public void close() {
        try {
            btSocket.close();
        } catch (IOException e) { }
    }

    public boolean isConnected() {
        if (btSocket == null) return false;
        else return btSocket.isConnected();
    }

    protected void changeState(int state) {
        mState = state;
    }
}
