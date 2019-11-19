package com.example.lanekeepassist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    public static final String CHANNEL_ID = "LKA";

    Button buttonConnect, buttonSend, buttonSettings;
    TextView txtReceived;
    EditText editTxtToSend;
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
        btHandler.setNotificationBuilder(notificationBuilder);
        btHandler.setNotificationManagerCompat(notificationManagerCompat);
        btHandler.setMainActivity(this);

        mBluetoothConnection = new BluetoothConnection();
        mBluetoothConnection.setHandler(btHandler);
        mBluetoothConnection.setMainActivity(this);

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

        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(intent);
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
        buttonSettings = (Button) findViewById(R.id.buttonSettings);
        txtReceived = (TextView) findViewById(R.id.textReceived);
        editTxtToSend = (EditText) findViewById(R.id.editText);
    }

    public void setTxtReceived(String str) {
        txtReceived.setText(str);
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



