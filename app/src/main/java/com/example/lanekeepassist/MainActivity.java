package com.example.lanekeepassist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "LKA";

    Button buttonConnect, buttonSend, buttonSettings;
//    TextView txtReceived;
//    EditText editTxtToSend;
    BluetoothHandler btHandler;

    private NotificationManagerCompat notificationManagerCompat = null;

    MediaPlayer mediaPlayer;

    private BluetoothConnection mBluetoothConnection;

    public Bitmap imageBitmap = null;
    public byte[] imageByteArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        createNotificationChannel();

        notificationManagerCompat = NotificationManagerCompat.from(this);

        btHandler = new BluetoothHandler();
        btHandler.setNotificationBuilder(notificationBuilder);
        btHandler.setNotificationManagerCompat(notificationManagerCompat);
        btHandler.setMainActivity(this);

        mBluetoothConnection = new BluetoothConnection();
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alert1);
        btHandler.setMediaPlayer(mediaPlayer);

        mBluetoothConnection.setHandler(btHandler);
        mBluetoothConnection.setMainActivity(this);

        mBluetoothConnection.checkState();

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBluetoothConnection.connect();
                mBluetoothConnection.start();

//                mBluetoothConnection.write("start");
            }
        });


//        buttonSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            if (mBluetoothConnection.isConnected()) {
//                String content = editTxtToSend.getText().toString();
//                mBluetoothConnection.write(content);
//                Toast.makeText(getBaseContext(), "Sent text", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getBaseContext(), "Device not connected", Toast.LENGTH_SHORT).show();
//            }
//            }
//        });



        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
//                intent.putExtra("ImageByteArray", imageByteArray);
//                startActivityForResult(intent, 0);
            if (mBluetoothConnection.isConnected()) {
                mBluetoothConnection.changeState(BluetoothConnection.STATE_IMAGE_RECEIVING);
                mBluetoothConnection.write("open_settings");
                buttonSettings.setEnabled(false);
            } else {
                Toast.makeText(getBaseContext(), "Device not connected", Toast.LENGTH_SHORT).show();
            }
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mBluetoothConnection.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        buttonSettings.setEnabled(true);
        //Retrieve data in the intent
        String settings = data.getStringExtra("settings");
        if (mBluetoothConnection.isConnected() && !settings.isEmpty()) {
            mBluetoothConnection.write(settings);
        }
    }

    public void initializeViews() {
        buttonConnect = findViewById(R.id.buttonConnect);
//        buttonSend = findViewById(R.id.buttonSendData);
        buttonSettings = findViewById(R.id.buttonSettings);
//        txtReceived = findViewById(R.id.textReceived);
//        editTxtToSend = findViewById(R.id.editText);
    }

//    public void setTxtReceived(String str) {
//        txtReceived.setText(str);
//    }

    public void setImageByteArray(byte[] ba) {
        imageByteArray = ba;
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
            .setContentTitle("Lane Departure Warning")
//            .setContentText("")
            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE) //Important for heads-up notification
            .setPriority(Notification.PRIORITY_MAX)
            .setStyle(new NotificationCompat.BigTextStyle());
}



