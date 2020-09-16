package com.cognition.android.mailboxapp.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.widget.Toast;

import com.cognition.android.mailboxapp.AutoDeleteSocialMails;
import com.cognition.android.mailboxapp.MyService;
import com.cognition.android.mailboxapp.R;
import com.cognition.android.mailboxapp.activity_swipe;
import com.cognition.android.mailboxapp.create_summary;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import java.util.Arrays;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.cognition.android.mailboxapp.activity_swipe.PREF_VIEW_TYPE;

public class MainActivity extends AppCompatActivity {

    AppCompatButton btnChooseAccount;
    GoogleAccountCredential mCredential;
    SharedPreferences sharedPref;

    public static final String TAG = "MailBoxApp";
    public static final String[] SCOPES = {GmailScopes.MAIL_GOOGLE_COM};
    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String AUTO_DELETE_SOCIAL = "autoDeleteSocial";
    public static final String PREFERRED_EMAIL_ADDRESSES = "preferredEmailAddresses";
    public static final String PREF_CATEGORIES = "preferencesList";
    public static final String ORGANIZATION_NAME = "organizationName";
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("FLOW", "MainActivity");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        sharedPref = MainActivity.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);

        String accountName = sharedPref.getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);

            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
            String viewType = sharedPreferences.getString(PREF_VIEW_TYPE,"List");

            if(sharedPref.getString(AUTO_DELETE_SOCIAL,"false").equals("true")){
                Intent intent = new Intent(MainActivity.this, AutoDeleteSocialMails.class);
                startService(intent);
            }
            else
            {
                Intent intent = new Intent(MainActivity.this, AutoDeleteSocialMails.class);;
                stopService(intent);
            }

            if(viewType.equals("List"))
            {
                startActivity(new Intent(MainActivity.this, InboxActivity.class));
                ActivityCompat.finishAffinity(MainActivity.this);
            }
            else if (viewType.equals("Swipe"))
            {
                startActivity(new Intent(MainActivity.this, activity_swipe.class));
                ActivityCompat.finishAffinity(MainActivity.this);
            }
        }

        btnChooseAccount = findViewById(R.id.btnChooseAccount);
        btnChooseAccount.setOnClickListener(v -> {
            if (!isGooglePlayServicesAvailable()) {
                acquireGooglePlayServices();
            } else {
                chooseAccount();
            }
        });
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.this_app_requires_google_play_services);
                    builder.setPositiveButton(getString(android.R.string.cancel), (dialog, which) -> {
                        dialog.dismiss();
                        ActivityCompat.finishAffinity(MainActivity.this);
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    chooseAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);

                        //Intent intent = new Intent(MainActivity.this, MyService.class);
                        //startService(intent);

                        //startService(new Intent(MainActivity.this, create_summary.class));

                        startActivity(new Intent(MainActivity.this, OrganizationActivity.class).putExtra("callingActivity","Main"));
                        ActivityCompat.finishAffinity(MainActivity.this);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    chooseAccount();
                }
                break;
        }
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            // Start a dialog from which the user can choose an account
            startActivityForResult(
                    mCredential.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER);
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}
