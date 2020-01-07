package com.example.lanekeepassist;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BluetoothHandler extends Handler {

    static final int STATE_ASSISTANT_RECEIVED = 1;
    static final int STATE_IMAGE_RECEIVED=2;
    static final int STATE_SETTINGS_RECEIVED=3;


    NotificationCompat.Builder notificationBuilder;
    NotificationManagerCompat notificationManagerCompat;

    private MainActivity mMainActivity;

    private SettingsActivity mSettingsActivity;

    MediaPlayer mediaPlayer;

    Intent settingsIntent;

    public void handleMessage(Message msg) {

        switch (msg.what) {
            case STATE_ASSISTANT_RECEIVED:
                Bundle bundle = msg.getData();
                String string = bundle.getString("assistant");
                String[] vals = string.split(" ");
                if (vals.length == 3 && vals[0].equals("alert")) {
                    mMainActivity.offsetText.setText(vals[2]);
                    // insignificant offset - straight icon
                    if (vals[1].equals("0")) {
                        mMainActivity.offsetIcon.setImageResource(R.drawable.straight);
                    }
                    // warning
                    else if (vals[1].equals("1")) {
                        //left
                        if (Integer.parseInt(vals[2]) > 0)
                            mMainActivity.offsetIcon.setImageResource(R.drawable.warn_left);
                        //right
                        else
                            mMainActivity.offsetIcon.setImageResource(R.drawable.warn_right);
                    }
                    // alert
                    else if (vals[1].equals("2")) {
                        //left
                        if (Integer.parseInt(vals[2]) > 0) {
                            notificationBuilder.setContentText("Off " + vals[2] + " m to the left");
                            mMainActivity.offsetIcon.setImageResource(R.drawable.alert_left);
                        }
                            //right
                        else {
                            notificationBuilder.setContentText("Off " + vals[2] + " m to the right");
                            mMainActivity.offsetIcon.setImageResource(R.drawable.alert_right);
                        }
                        Notification notification = notificationBuilder.build();
                        notificationManagerCompat.notify(0, notification);
                        mediaPlayer.start();
                    }
//                    if (vals[1].equals("R")) {
//                        notificationBuilder.setContentText("Off " + vals[2] + " m to the right");
//                    } else {
//                        notificationBuilder.setContentText("Off " + vals[2] + " m to the left");
//
//                    }
//                    Notification notification = notificationBuilder.build();
//                    notificationManagerCompat.notify(0, notification);
//                    mediaPlayer.start();
                }
//                if (string.matches("^[a-zA-Z0-9]*$")) {
////                    mMainActivity.setTxtReceived(string);
//                    if (string.equals("alert")) {
//
//                    }
//                }
                break;

            case STATE_IMAGE_RECEIVED:
                byte[] readBuff= (byte[]) msg.obj;
                mMainActivity.setImageByteArray(readBuff);
                settingsIntent = new Intent(mMainActivity, SettingsActivity.class);
                settingsIntent.putExtra("ImageByteArray", readBuff);
//                Intent intent = new Intent(mMainActivity, SettingsActivity.class);
//                intent.putExtra("ImageByteArray", readBuff);
//                mMainActivity.startActivityForResult(intent, 0);
//                Bitmap bitmap= BitmapFactory.decodeByteArray(readBuff,0,msg.arg1);
//                mMainActivity.setImageBitmap(bitmap);
//                mSettingsActivity.imageView.setImageBitmap(bitmap);
                break;

            case STATE_SETTINGS_RECEIVED:
                Bundle bundleSettings = msg.getData();
                String settings = bundleSettings.getString("settings");
//                String[] data = settings.split(" ");
//                mSettingsActivity.et_BLx.setText(data[1]);
                settingsIntent.putExtra("Settings", settings);
                mMainActivity.startActivityForResult(settingsIntent, 0);
                break;
        }


    }

    void setNotificationBuilder(NotificationCompat.Builder builder) {
        notificationBuilder = builder;
    }

    void setNotificationManagerCompat(NotificationManagerCompat nmp) {
        notificationManagerCompat = nmp;
    }

    void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    void setMediaPlayer(MediaPlayer mp) {
        mediaPlayer = mp;
    }

    void setSettingsActivity(SettingsActivity settingsActivity) {
        mSettingsActivity = settingsActivity;
    }


}
