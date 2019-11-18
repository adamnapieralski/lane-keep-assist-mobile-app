package com.example.lanekeepassist;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {

    private BluetoothAdapter btAdapter = null;
    private BluetoothDevice btDevice = null;
    private BluetoothSocket btSocket = null;

    public InputStream inputStream;
    public OutputStream outputStream;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    // String for MAC address
    private static final String deviceMAC = "DC:A6:32:3C:18:BE";

    private void writeToLogs(String message) {
        Log.d("HelloServices", message);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btDevice = btAdapter.getRemoteDevice(deviceMAC);


        try {
            btSocket = createBluetoothSocket(btDevice);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            //Create I/O streams for connection
            tmpIn = btSocket.getInputStream();
            tmpOut = btSocket.getOutputStream();
        } catch (IOException e) { }

        inputStream = tmpIn;
        outputStream = tmpOut;

        try {
            outputStream.write("start".getBytes());
        } catch (IOException e) {
            //if you cannot write, close the application
            Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        writeToLogs("Called onStartCommand() method");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }
}
