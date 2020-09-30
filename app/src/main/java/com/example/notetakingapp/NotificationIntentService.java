package com.example.notetakingapp;

import android.app.IntentService;
import android.app.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import timber.log.Timber;

import static android.os.Build.*;
import static androidx.core.app.NotificationCompat.*;


/**
 * Created by ras on 8/11/2017.
 */

@RequiresApi(api = VERSION_CODES.O)
public class NotificationIntentService<SDK_INT> extends IntentService {
    private static final int NOTIFICATION_ID=001;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    Notification notification;
    public NotificationIntentService() {
        super("Notification");
    }
    public String CHANNEL_ID = "my_channel_01";
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String text = intent.getStringExtra("item");

        Context context = this.getApplicationContext();
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent mIntent = new Intent(this,MainActivity.class );
        Bundle bundle = new Bundle();
        bundle.putString("message", text);
        mIntent.putExtras(bundle);
        pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentTitle(getString(R.string.reminder))
                .setContentText(text).build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        notification.ledARGB = 0xFFFFA500;
        notification.ledOnMS = 800;
        notification.ledOffMS = 1000;
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
        Timber.i("Notifications sent.");
    }

}
