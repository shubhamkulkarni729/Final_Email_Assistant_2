package com.cognition.android.mailboxapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cognition.android.mailboxapp.MyService;
import com.cognition.android.mailboxapp.R;
import com.cognition.android.mailboxapp.settings;

import static com.cognition.android.mailboxapp.activities.MainActivity.ORGANIZATION_NAME;

public class OrganizationActivity extends AppCompatActivity {

    TextView labelOrganization;
    EditText textOrganization;
    Button btnOrganizationSave;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization);
        String callingActivity = getIntent().getStringExtra("callingActivity");

        final Intent intent = new Intent(OrganizationActivity.this, MyService.class);
        stopService(intent);

        sharedPref = OrganizationActivity.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        String previousOrganization = sharedPref.getString(ORGANIZATION_NAME,"");

        Log.d("FLOW", "OrganizationActivity");

        labelOrganization = findViewById(R.id.labelOrganization);
        textOrganization = findViewById(R.id.textOrganizationName);
        btnOrganizationSave = findViewById(R.id.btnSaveOrganization);
        textOrganization.setHint("Edit organization domain name: "+previousOrganization);

        btnOrganizationSave.setOnClickListener(v -> {

            if(labelOrganization.getText().toString() == null) {
                Toast.makeText(OrganizationActivity.this,"Please enter the organization name", Toast.LENGTH_SHORT).show();
            }
            else {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(ORGANIZATION_NAME,textOrganization.getText().toString().trim());
                editor.apply();

                Toast.makeText(OrganizationActivity.this,"Organization domain name saved as : "+sharedPref.getString(ORGANIZATION_NAME,""),Toast.LENGTH_LONG).show();

                if(callingActivity.equals("Settings")){
                    startService(intent);
                    finish();
                }
                else {
                    startActivity(new Intent(OrganizationActivity.this, PreferenceListActivity.class).putExtra("callingActivity","Organization"));
                    ActivityCompat.finishAffinity(OrganizationActivity.this);
                }

            }
        });
    }

}
