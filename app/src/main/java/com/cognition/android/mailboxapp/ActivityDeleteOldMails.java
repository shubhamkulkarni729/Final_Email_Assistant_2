package com.cognition.android.mailboxapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;
import static com.cognition.android.mailboxapp.activities.MainActivity.SCOPES;

public class ActivityDeleteOldMails extends AppCompatActivity {

    GoogleAccountCredential mCredential;
    Gmail mService;
    SharedPreferences sharedPref;
    FirebaseDatabase database;
    String accountName;
    DatabaseReference myRef;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_old_mails);
        spinner = (ProgressBar)findViewById(R.id.progressBar);
        TextView textView = findViewById(R.id.textViewSpinnerLabel);

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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityDeleteOldMails.this);
        alertDialogBuilder.setMessage("Are you sure to delete old mails ?");

        alertDialogBuilder.setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //Intent intent = new Intent(getContext(),ServiceDeleteOldMails.class);
                spinner.setVisibility(View.VISIBLE);
                delete();
                spinner.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                Toast.makeText(ActivityDeleteOldMails.this,"Deleted Successfully",Toast.LENGTH_LONG).show();
                finish();
            }
        });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private int delete(){
        Calendar calNow = Calendar.getInstance();
        calNow.add(Calendar.YEAR, -1);
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
                                String id = firebaseMessage.getId();
                                Message mMessage = SQLite.select().from(Message.class).where(Message_Table.message_id.eq(id)).querySingle();
                                if(mMessage!=null) {
                                    if (mMessage.getTimestamp() < dateRequired && !firebaseMessage.getCategory().toLowerCase().contains("primary") && !firebaseMessage.getCategory().toLowerCase().contains("organization")) {
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
        return 1;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
