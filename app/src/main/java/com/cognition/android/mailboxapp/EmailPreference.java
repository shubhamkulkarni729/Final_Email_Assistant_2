package com.cognition.android.mailboxapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.cognition.android.mailboxapp.models.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.HashSet;
import java.util.List;

import static com.cognition.android.mailboxapp.activities.MainActivity.PREFERRED_EMAIL_ADDRESSES;

public class EmailPreference extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_preference);
        sharedPreferences = EmailPreference.this.getSharedPreferences("com.cognition.android.mailboxapp.PREFERENCES_FILE", Context.MODE_PRIVATE);
        String preferredAddresses = sharedPreferences.getString(PREFERRED_EMAIL_ADDRESSES,"");

        ListView listView = findViewById(R.id.email_list_view);
        listView.addHeaderView(new View(this));
        listView.addFooterView(new View(this));


        List<com.cognition.android.mailboxapp.models.Message> messages = SQLite.select().from(com.cognition.android.mailboxapp.models.Message.class).queryList();
        EmailPreferenceListAdapter emailPreferenceListAdapter = new EmailPreferenceListAdapter(getApplicationContext(), R.layout.email_list);


        HashSet<String> hashSet = new HashSet<>();

        for(Message message: messages)
        {
            hashSet.add(message.getFrom());
        }

        boolean preferred = false;
        for(String email: hashSet)
        {
            if(preferredAddresses.contains(email)) preferred = true;
            else  preferred = false;

            EmailPreferenceObject emailPreferenceObject = new EmailPreferenceObject(email,preferred);
            emailPreferenceListAdapter.add(emailPreferenceObject);
        }

        listView.setAdapter(emailPreferenceListAdapter);
    }
}