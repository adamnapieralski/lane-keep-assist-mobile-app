package com.example.lanekeepassist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    public static final String CHANNEL_ID = "LKA";

    Button buttonConnect, buttonSend;
    TextView txtReceived;
    EditText editTxtToSend;
    ImageView imageView;
    BluetoothHandler btHandler;

    private NotificationManagerCompat notificationManagerCompat = null;

    MediaPlayer mediaPlayer;

    private BluetoothConnection mBluetoothConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        createNotificationChannel();

        notificationManagerCompat = NotificationManagerCompat.from(this);

        btHandler = new BluetoothHandler();

        mBluetoothConnection = new BluetoothConnection();
        mBluetoothConnection.setHandler(btHandler);
        mBluetoothConnection.setmMainActivity(this);

        mBluetoothConnection.checkState();

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alert1);

                mBluetoothConnection.connect();
                mBluetoothConnection.start();
                mBluetoothConnection.write("start");
            }
        });


        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editTxtToSend.getText().toString();
                mBluetoothConnection.write(content);
                Toast.makeText(getBaseContext(), "Sent text", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mBluetoothConnection.close();
    }

    public void initializeViews() {
        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonSend = (Button) findViewById(R.id.buttonSendData);
        txtReceived = (TextView) findViewById(R.id.textReceived);
        editTxtToSend = (EditText) findViewById(R.id.editText);
        imageView = (ImageView) findViewById(R.id.imageView);
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
            Bundle bundle = msg.getData();
            String string = bundle.getString("key");
            txtReceived.setText(string);

            Notification notification = notificationBuilder.build();
            notificationManagerCompat.notify(0, notification);
//            mediaPlayer.start();

//            byte[] readBuff= (byte[]) msg.obj;
//            Bitmap bitmap=BitmapFactory.decodeByteArray(readBuff,0,msg.arg1);
//
//            imageView.setImageBitmap(bitmap);
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



