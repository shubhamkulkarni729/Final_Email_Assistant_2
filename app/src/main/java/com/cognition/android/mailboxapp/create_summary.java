package com.cognition.android.mailboxapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import com.cognition.android.mailboxapp.Summary_Utils.Summary;
import com.cognition.android.mailboxapp.activities.MainActivity;
import com.cognition.android.mailboxapp.models.Message;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import static com.cognition.android.mailboxapp.activities.MainActivity.TAG;

public class create_summary extends Service {
    boolean isRunning;
    List<Message> messages;
    FirebaseDatabase database;
    DatabaseReference myRef;

    public create_summary(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        messages = SQLite.select().from(Message.class).queryList();
        isRunning = true;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("summary");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(() -> {
            while(true) {
                try {
                    if (isNetworkConnected()) {
                        for (Message message : messages) {
                            message.setSummary(Summary.summarize(message.getBody(), message.getSubject(),getApplicationContext()));
                            message.update();
                            myRef.child(String.valueOf(message.getId())).setValue(message.getSummary());
                        }
                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    Log.w(MainActivity.TAG, e);
                    e.printStackTrace();
                }
                if(isRunning){
                    Log.d(TAG, "Summary Service running");
                }
            }
        }).start();

        return Service.START_STICKY;
    }
    @Override
    public void onDestroy() {
        isRunning = false;
        Log.i(TAG, "Service onDestroy");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}