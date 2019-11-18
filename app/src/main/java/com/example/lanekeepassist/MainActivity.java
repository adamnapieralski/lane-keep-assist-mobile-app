package com.example.lanekeepassist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    public static final String CHANNEL_ID = "LKA";

    BluetoothManager bluetoothManager = new BluetoothManager();

    final int handlerState = 0;

    Button buttonConnect, buttonSend;
    TextView txtReceived;
    EditText editTxtToSend;
    ImageView imageView;
    Handler btHandler;

    private BluetoothAdapter btAdapter = null;
    private BluetoothDevice btDevice = null;
    private BluetoothSocket btSocket = null;

    public InputStream inputStream;
    public OutputStream outputStream;

//    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    // String for MAC address
    private static final String deviceMAC = "DC:A6:32:3C:18:BE";

    private NotificationManagerCompat notificationManagerCompat = null;

    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        createNotificationChannel();

        notificationManagerCompat = NotificationManagerCompat.from(this);

        btHandler = new BluetoothHandler();

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btDevice = btAdapter.getRemoteDevice(deviceMAC);

        checkBTState();

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

//                InputStream tmpIn = null;
//                OutputStream tmpOut = null;
//
//                try {
//                    //Create I/O streams for connection
//                    tmpIn = btSocket.getInputStream();
//                    tmpOut = btSocket.getOutputStream();
//                } catch (IOException e) { }
//
//                inputStream = tmpIn;
//                outputStream = tmpOut;



//                BluetoothService serv = new BluetoothService();
//                startService(new Intent(v.getContext(), BluetoothService.class));

                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alert1);
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();

                //I send a character when resuming.beginning transmission to check device is connected
                //If it is not an exception will be thrown in the write method and finish() will be called
                mConnectedThread.write("start");
            }
        });


        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editTxtToSend.getText().toString();
                mConnectedThread.write(content);
                Toast.makeText(getBaseContext(), "Sent text", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

//    public void connectToPi(View view){
//        bluetoothManager.initializeSocket();
//        bluetoothManager.connectSocket();
//        bluetoothManager.initializeStreams();
//        bluetoothManager.start();
//    }
//
//    public void sendData(View view){
//        byte[] byteArr = {'B'};
//        bluetoothManager.write(byteArr);
//    }

    public void initializeViews() {
        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonSend = (Button) findViewById(R.id.buttonSendData);
        txtReceived = (TextView) findViewById(R.id.textReceived);
        editTxtToSend = (EditText) findViewById(R.id.editText);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
//            byte[] buffer = new byte[512];
//            int bytes;


            byte[] buffer = null;
            int numberOfBytes = 0;
            int index=0;
            boolean flag = true;



            // Keep looping to listen for received messages
//            while (true) {
//                try {
//                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
//                    String readMessage = new String(buffer, 0, bytes);
//                    Message msg = btHandler.obtainMessage();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("key", readMessage);
//                    msg.setData(bundle);
//                    btHandler.sendMessage(msg);
//
//                } catch (IOException e) {
//                    break;
//                }
//            }

            while (true) {
                if (flag) {
                    try {
                        byte[] temp = new byte[mmInStream.available()];
                        if(mmInStream.read(temp)>0)
                        {
                            numberOfBytes=Integer.parseInt(new String(temp,"UTF-8"));
                            buffer=new byte[numberOfBytes];
                            flag=false;
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
                else {
                    try {
                        byte[] data=new byte[mmInStream.available()];
                        int numbers=mmInStream.read(data);

                        System.arraycopy(data,0,buffer,index,numbers);
                        index=index+numbers;

                        if(index == numberOfBytes)
                        {
                            btHandler.obtainMessage(0,numberOfBytes,-1,buffer).sendToTarget();
                            flag = true;
                        }
                    } catch (IOException e) {
                        break;
                    }
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
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    public class BluetoothHandler extends Handler {
        public void handleMessage(Message msg) {
//            Bundle bundle = msg.getData();
//            String string = bundle.getString("key");
//            txtReceived.setText(string);
//
//            Notification notification = notificationBuilder.build();
//            notificationManagerCompat.notify(0, notification);
//            mediaPlayer.start();

            byte[] readBuff= (byte[]) msg.obj;
            Bitmap bitmap=BitmapFactory.decodeByteArray(readBuff,0,msg.arg1);

            imageView.setImageBitmap(bitmap);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
            MainActivity.this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_drive_eta_black_24dp)
            .setContentTitle("LKA notification")
            .setContentText("Much longer text that cannot fit one line...")
            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE) //Important for heads-up notification
            .setPriority(Notification.PRIORITY_MAX)
            .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText("Much longer text that cannot fit one line..."));

}



