package com.cognition.android.mailboxapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cognition.android.mailboxapp.R;


public class Notification_receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Display Notification
        NotificationManager notificationManager =(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeating_intent = new Intent(context, activity_swipe.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,100,repeating_intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.notification);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"notify")
                .setContentIntent(pendingIntent)
                .setContentTitle("Email Assistant")
                .setSmallIcon(android.R.drawable.arrow_up_float)
                .setLargeIcon(icon)
                .setContentText("Check Email Assistant to see new emails")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //notificationManager.notify(100,builder.build());
        if(intent.getAction().equals("MY_NOTIFICATION_MESSAGE")){
            notificationManager.notify(100,builder.build());
            Log.i("Notify","Alarm");
        }
    }
}