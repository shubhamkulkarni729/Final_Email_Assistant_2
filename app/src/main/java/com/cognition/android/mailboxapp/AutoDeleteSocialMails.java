package com.cognition.android.mailboxapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cognition.android.mailboxapp.models.Message;
import com.cognition.android.mailboxapp.models.Message_Table;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;
import static com.cognition.android.mailboxapp.activities.MainActivity.SCOPES;
import static com.cognition.android.mailboxapp.activities.MainActivity.TAG;

public class AutoDeleteSocialMails extends Service {

    GoogleAccountCredential mCredential;
    Gmail mService;
    SharedPreferences sharedPref;
    FirebaseDatabase database;
    String accountName;
    DatabaseReference myRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        Log.d(TAG, "Service onCreate");
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mService = null;

        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        accountName = sharedPref.getString(PREF_ACCOUNT_NAME, null);

        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("MailBox App")
                    .build();
        }

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("messages/" + (accountName.hashCode()));
        System.out.println("SERVICE ONCREATE");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        System.out.println("SERVICE ONSTARTCOMMAND");
        Calendar calNow = Calendar.getInstance();
        calNow.add(Calendar.MONTH, -2);
        Date dateBeforeTwoMonths = calNow.getTime();
        long dateRequired = dateBeforeTwoMonths.getTime();
        new Thread(()->{
                if(isNetworkConnected()){
                    try{
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot d : dataSnapshot.getChildren()){
                                    FirebaseMessage firebaseMessage = d.getValue(FirebaseMessage.class);
                                    String id = firebaseMessage.id;
                                    Message mMessage = SQLite.select().from(Message.class).where(Message_Table.message_id.eq(id)).querySingle();
                                    if(mMessage!=null) {
                                        if (mMessage.getTimestamp() < dateRequired && mMessage.getLabelsJson().contains("CATEGORY_SOCIAL")) {
                                            dataSnapshot.getRef().removeValue();    //deleting firebase record
                                            SQLite.delete().from(Message.class).where(Message_Table.message_id.eq(id)).query(); //deleting sqlite record
                                            try {
                                                mService.users().messages().trash("me", mMessage.getMessage_id());  //deleting gmail record
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            System.out.println("Removed" + id);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    catch (Exception e){

                    }
                }

        }).start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.d("Service","Destroyed");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
