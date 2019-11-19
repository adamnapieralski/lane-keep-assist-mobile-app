package com.example.lanekeepassist;

import android.app.Activity;
import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


    public void handleMessage(Message msg) {

        switch (msg.what) {
            case STATE_ASSISTANT_RECEIVED:
                Bundle bundle = msg.getData();
                String string = bundle.getString("key");
                mMainActivity.setTxtReceived(string);
                Notification notification = notificationBuilder.build();
                notificationManagerCompat.notify(0, notification);
                //play mediaplayer
                break;

            case STATE_IMAGE_RECEIVED:
                byte[] readBuff= (byte[]) msg.obj;
                Bitmap bitmap= BitmapFactory.decodeByteArray(readBuff,0,msg.arg1);

//                imageView.setImageBitmap(bitmap);
                break;

            case STATE_SETTINGS_RECEIVED:
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

//    void setSetttingsActivity(Activity settingsActivity) {
//        mSettingsActivity = settingsActivity;
//    }


}
