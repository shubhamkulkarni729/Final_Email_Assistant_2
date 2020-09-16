package com.cognition.android.mailboxapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.cognition.android.mailboxapp.MyService;
import com.cognition.android.mailboxapp.R;
import com.cognition.android.mailboxapp.activity_swipe;


public class PreferenceListActivity extends AppCompatActivity {

    CheckBox checkBoxPrimary;
    CheckBox checkBoxSocial;
    CheckBox checkBoxJobs;
    CheckBox checkBoxEducational;
    CheckBox checkBoxFinancial;
    CheckBox checkBoxOffers;
    CheckBox checkBoxOrganizational;
    Button btnSave;

    String callingActivity;

    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_list);

        Log.d("FLOW", "PreferenceActivity");

        callingActivity = getIntent().getStringExtra("callingActivity");

        Intent intent = new Intent(PreferenceListActivity.this, MyService.class);
        stopService(intent);

        checkBoxPrimary = findViewById(R.id.checkBoxPrimary);
        checkBoxSocial = findViewById(R.id.checkBoxSocial);
        checkBoxJobs = findViewById(R.id.checkBoxJobs);
        checkBoxEducational = findViewById(R.id.checkBoxEducational);
        checkBoxFinancial = findViewById(R.id.checkBoxFinancial);
        checkBoxOffers = findViewById(R.id.checkBoxOffers);
        checkBoxOrganizational = findViewById(R.id.checkBoxOrganizational);

        btnSave = findViewById(R.id.btnSavePreferencesList);
        sharedPref = PreferenceListActivity.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        btnSave.setOnClickListener(v -> setPreferencesList());
    }

    private void setPreferencesList(){

        boolean primary = checkBoxPrimary.isChecked();
        boolean social = checkBoxSocial.isChecked();
        boolean jobs = checkBoxJobs.isChecked();
        boolean educational = checkBoxEducational.isChecked();
        boolean financial = checkBoxFinancial.isChecked();
        boolean offers = checkBoxOffers.isChecked();
        boolean organizational = checkBoxOrganizational.isChecked();

        String preferencesList = "";
        if(primary) preferencesList = preferencesList.concat("primary,");
        if(social) preferencesList = preferencesList.concat("social,");
        if(jobs) preferencesList = preferencesList.concat("jobs,");
        if(educational) preferencesList = preferencesList.concat("educational,");
        if(financial) preferencesList = preferencesList.concat("financial,");
        if(offers) preferencesList = preferencesList.concat("offers,");
        if(organizational) preferencesList = preferencesList.concat("organizational");

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("preferencesList",preferencesList);
        editor.apply();

        Toast.makeText(PreferenceListActivity.this,"Preference categories saved.",Toast.LENGTH_LONG).show();

        Intent intent = new Intent(PreferenceListActivity.this, MyService.class);
        startService(intent);

        if(callingActivity.equals("Organization")) {
            Intent in = new Intent(PreferenceListActivity.this, activity_swipe.class);
            startActivity(in);
            ActivityCompat.finishAffinity(PreferenceListActivity.this);
        }

        else {
            finish();
        }
    }
}