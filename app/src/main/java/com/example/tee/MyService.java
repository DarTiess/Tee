package com.example.tee;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    NotificationManager notificationManager;

    public void onCreate() {
        super.onCreate();


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        try{
            TimeUnit.SECONDS.sleep(5);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendNotif();
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotif() {

Intent intent=new Intent(this, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder=
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Teeeeee")
                .setContentText("I want teeee")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Notification notification=builder.build();

        NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification);
    }


    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }
}
