package com.cognition.android.mailboxapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.CallableStatement;
import java.util.Date;
import java.util.HashMap;

import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;

public class smart_insights extends AppCompatActivity {

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_insights);

        sharedPref = getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        String name = sharedPref.getString(PREF_ACCOUNT_NAME,"Account Name");

        LinearLayout linearLayout = findViewById(R.id.insights);

        TextView txt = new TextView(this);
        txt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        txt.setText("Credited Amount");
        txt.setTextSize(32);
        linearLayout.addView(txt);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Transaction/"+name.hashCode()+"/Credit");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren()) {
                    String name = d.getKey();
                    HashMap transaction = (HashMap) d.getValue();

                    TextView txt3 = new TextView(getApplicationContext());
                    txt3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    txt3.setText(name);
                    txt3.setTextSize(24);
                    linearLayout.addView(txt3);

                    for(Object sender : transaction.values()) {

                        HashMap<String, Transaction> trans = (HashMap<String, Transaction>) sender;

                        TextView txt4 = new TextView(getApplicationContext());
                        txt4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        txt4.setText(trans.get("amount")+ "\t" + trans.get("date"));
                        txt4.setTextSize(20);
                        txt4.setTextColor(Color.GREEN);
                        linearLayout.addView(txt4);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference = firebaseDatabase.getReference("Transaction/"+name.hashCode()+"/Debit");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView txt2 = new TextView(getApplicationContext());
                txt2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                txt2.setText("Debited Amount");
                txt2.setTextSize(32);
                linearLayout.addView(txt2);

                for(DataSnapshot d : dataSnapshot.getChildren()) {
                    String name = d.getKey();
                    HashMap transaction = (HashMap) d.getValue();

                    TextView txt3 = new TextView(getApplicationContext());
                    txt3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    txt3.setText(name);
                    txt3.setTextSize(24);
                    linearLayout.addView(txt3);

                    for (Object sender : transaction.values()) {
                        HashMap<String, Transaction> trans = (HashMap<String, Transaction>) sender;

                        TextView txt4 = new TextView(getApplicationContext());
                        txt4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        txt4.setText(trans.get("amount") + "\t" + trans.get("date"));
                        txt4.setTextSize(20);
                        txt4.setTextColor(Color.RED);
                        linearLayout.addView(txt4);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}