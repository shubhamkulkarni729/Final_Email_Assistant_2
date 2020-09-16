package com.cognition.android.mailboxapp;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class PaymentAppChoserActivity extends AppCompatActivity {

    FloatingActionButton fabPhonepe;
    FloatingActionButton fabGooglePay;
    FloatingActionButton fabPaytm;
    FloatingActionButton fabAmazonPay;
    FloatingActionButton fabFreecharge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_app_choser);

        fabPhonepe = findViewById(R.id.btnPhonepe);
        fabGooglePay = findViewById(R.id.btnGooglePay);
        fabPaytm = findViewById(R.id.btnPaytm);
        fabAmazonPay = findViewById(R.id.btnAmazonPay);
        fabFreecharge = findViewById(R.id.btnFreecharge);

        fabPhonepe.setOnClickListener(v -> openApp("com.phonepe.app"));

        fabGooglePay.setOnClickListener(v -> openApp("com.google.android.apps.nbu.paisa.user"));

        fabPaytm.setOnClickListener(v -> openApp("net.one97.paytm"));

        fabAmazonPay.setOnClickListener(v -> openApp("in.amazon.mShop.android.shopping"));

        fabFreecharge.setOnClickListener(v -> openApp("com.freecharge.android"));
    }

    private void openApp(String packageName)
    {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(PaymentAppChoserActivity.this, "The app is not installed", Toast.LENGTH_LONG).show();
        }
    }
}