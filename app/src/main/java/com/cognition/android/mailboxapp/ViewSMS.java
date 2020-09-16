package com.cognition.android.mailboxapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cognition.android.mailboxapp.activities.ComposeActivity;
import com.cognition.android.mailboxapp.activities.EventsActivity;
import com.cognition.android.mailboxapp.activities.InboxActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.rationalminds.LocalDateModel;
import net.rationalminds.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;


public class ViewSMS extends AppCompatActivity {

    Button btnSent, btnInbox, btnPay;
    TextView textViewEmail;

    private SMSCardAdapter cardArrayAdapter;
    //private DraftCardArrayAdapter draftCardArrayAdapter;
    private ListView listView;
    private SearchView mSearchView;
    SharedPreferences sharedPref;
    private static final int RC_PERM_SMS = 125;
    String[] perms = {Manifest.permission.READ_SMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sms);
        //nav bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        Spinner spinner = findViewById(R.id.spinner_sms);
        List<String> list = new ArrayList<>();
        list.add("No Filter");
        list.add("Bills");
        list.add("Credit Transactions");
        list.add("Debit Transactions");
        list.add("Offer Codes");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Toast.makeText(ViewSMS.this,"Loading...",Toast.LENGTH_LONG).show();
                        cardArrayAdapter.clear();
                        showSMS(0);
                        break;

                    case 1:
                        Toast.makeText(ViewSMS.this,"Loading...",Toast.LENGTH_LONG).show();
                        cardArrayAdapter.clear();
                        showSMS(1);
                        break;

                    case 2:
                        Toast.makeText(ViewSMS.this,"Loading...",Toast.LENGTH_LONG).show();
                        cardArrayAdapter.clear();
                        showSMS(2);
                        break;

                    case 3:
                        Toast.makeText(ViewSMS.this,"Loading...",Toast.LENGTH_LONG).show();
                        cardArrayAdapter.clear();
                        showSMS(3);
                        break;

                    case 4:
                        Toast.makeText(ViewSMS.this,"Loading...",Toast.LENGTH_LONG).show();
                        cardArrayAdapter.clear();
                        showSMS(4);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        sharedPref = ViewSMS.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);

        View header = navigationView.getHeaderView(0);
        textViewEmail = (TextView)header.findViewById(R.id.nav_header_email);
        textViewEmail.setText(sharedPref.getString(PREF_ACCOUNT_NAME,"Account Name"));
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId())
            {
                case R.id.swipe_view:
                    Intent intent = new Intent(ViewSMS.this, activity_swipe.class);
                    startActivity(intent);
                    break;

                case R.id.insight_parent:
                    intent = new Intent(ViewSMS.this, smart_insights.class);
                    startActivity(intent);
                    break;

                case R.id.mailbox:
                    intent = new Intent(ViewSMS.this, InboxActivity.class);
                    startActivity(intent);
                    break;

                case R.id.nav_settings:
                    intent = new Intent(ViewSMS.this, settings.class);
                    startActivity(intent);
                    break;

                case R.id.events:
                    intent = new Intent(ViewSMS.this, EventsActivity.class);
                    startActivity(intent);
                    break;

                case R.id.sms:
                    intent = new Intent(ViewSMS.this, ViewSMS.class);
                    startActivity(intent);
                    break;

                case R.id.compose_email:
                    intent = new Intent(ViewSMS.this, ComposeActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.openNavDrawer, R.string.closeNavDrawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        listView = findViewById(R.id.card_listView);

        listView.addHeaderView(new View(this));
        listView.addFooterView(new View(this));
        //btnDraft = findViewById(R.id.btnDraft);
        //btnDraft.setOnClickListener(this);

        btnPay = findViewById(R.id.billbtn);
        cardArrayAdapter = new SMSCardAdapter(getApplicationContext(), R.layout.list_item_card);
        //sentArrayAdapter = new SentCardArrayAdapter(getApplicationContext(), R.layout.list_item_card);
        //mSearchView= findViewById(R.id.searchView1);

        if(!EasyPermissions.hasPermissions(ViewSMS.this, perms))
        {
            EasyPermissions.requestPermissions(ViewSMS.this, "This app needs access to SMS", RC_PERM_SMS, perms);
        }

        else {
            showSMS(0);
        }
        //draftCardArrayAdapter = new DraftCardArrayAdapter(getApplicationContext(), R.layout.list_item_card);

    }


    public void showSMS(int selection) {
        Log.d("SMS","SELECTION "+Integer.toString(selection));

        String re = "(?:Rs\\.?|INR)\\s*(\\d+(?:[.,]\\d+)*)|(\\d+(?:[.,]\\d+)*)\\s*(?:Rs\\.?|INR)";
        Pattern credittransaction = Pattern.compile("\\bcredited|Credited|CREDITED\\b");
        Pattern debittransaction = Pattern.compile("\\bdebited|Debited|DEBITED\\b");
        Pattern offercode = Pattern.compile("\\bcode|Code|CODE\\b");
        Pattern bill = Pattern.compile("\\bBill|bill|BILL\\b");
        Pattern amount = Pattern.compile(re);
        String o = "(?<=\\s|^)[A-Z0-9]{2,}(?=\\s|$)";
        Pattern offer = Pattern.compile(o);

         {

            cardArrayAdapter = null;    //clear method does not work on custom adapter
            cardArrayAdapter = new SMSCardAdapter(getApplicationContext(), R.layout.list_item_card);
            //listView.setAdapter(null);
            cardArrayAdapter.notifyDataSetChanged();
            // Create Draft box URI
            Uri draftURI = Uri.parse("content://sms/inbox");

            // List required columns
            String[] reqCols = new String[]{"_id", "address", "body"};

            // Get Content Resolver object, which will deal with Content
            // Provider
            ContentResolver cr = getContentResolver();

            // Fetch Sent SMS Message from Built-in Content Provider
            Cursor inboxcursor = cr.query(draftURI, reqCols, null, null, null);

            // Attached Cursor with adapter and display in listview
            String body = "", address = "", label = "Namaste", value = "";

            String name = sharedPref.getString(PREF_ACCOUNT_NAME,"Account Name");
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("Transaction/"+name.hashCode());

            int i = 0;

            //TODO Add threading and remove i

            if (inboxcursor.moveToFirst()) {
                while (i<100) {
                    int found = 0;
                    int credit = 0;
                    int debit = 0;

                    value = "";
                    label = "";
                    String id = inboxcursor.getString(inboxcursor.getColumnIndex("_id"));
                    body = inboxcursor.getString(inboxcursor.getColumnIndex("body"));
                    address = inboxcursor.getString(inboxcursor.getColumnIndex("address"));
                    String avl_balance = "";
                    // do what ever you want here

                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
                    String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

                    String contactName = "";
                    Cursor cur = getContentResolver().query(uri, projection, null, null, null);

                    if (cur != null) {
                        if (cur.moveToFirst()) {
                            contactName = cur.getString(0);
                        }
                        cur.close();
                    }

                    Matcher matcher = credittransaction.matcher(body);
                    if (matcher.find()) {

                        label = matcher.group() + " : ";
                        matcher = amount.matcher(body);
                        if (matcher.find()) {
                            value = matcher.group();
                            if(matcher.find()) {
                                avl_balance = matcher.group();
                            }
                            found = 1;
                            credit = 1;
                        }
                        else label = "";

                    }
                    matcher = debittransaction.matcher(body);
                    if (matcher.find() && found == 0) {
                        label = matcher.group() + " : ";
                        matcher = amount.matcher(body);
                        if (matcher.find()) {
                            value = matcher.group();
                            if(matcher.find()) {
                                avl_balance = matcher.group();
                            }
                            debit = 1;
                            found = 1;
                        }
                        else label = "";
                    }

                    matcher = offercode.matcher(body);
                    if (matcher.find() && found == 0) {
                        label = matcher.group() + " : ";
                        matcher = offer.matcher(body);
                        if (matcher.find()) {
                            value = matcher.group();
                            found = 1;
                        }
                        else label = "";
                    }
                    matcher = bill.matcher(body);
                    if (matcher.find() && found == 0) {
                        label = matcher.group() + " : ";
                        matcher = amount.matcher(body);
                        if (matcher.find()) {
                            value = matcher.group();
                            found = 1;
                        }
                        else label="";
                    }

                    if(contactName.equals(""))
                    {
                        contactName = address;
                    }

                    try {
                        String datePresent = checkForDate(body);
                        SMS_Card card;

                        if(!datePresent.equals(""))
                        {
                            card = new SMS_Card(contactName, label.toUpperCase() + value, body, btnPay, "Date : " + datePresent, avl_balance);
                            if(selection == 0) cardArrayAdapter.add(card);
                            if(selection == 1 && label.toUpperCase().contains("BILL"))  cardArrayAdapter.add(card);
                            if(selection == 2 && label.toUpperCase().contains("CREDIT"))    cardArrayAdapter.add(card);
                            if(selection == 3 && label.toUpperCase().contains("DEBIT")) cardArrayAdapter.add(card);
                            if(selection == 4 && label.toUpperCase().contains("CODE"))  cardArrayAdapter.add(card);

                            if(credit == 1) {
                                databaseReference.child("Credit").child(address).child(String.valueOf(datePresent.hashCode())).setValue(new Transaction(value,datePresent));
                            }
                            if(debit == 1){
                                databaseReference.child("Debit").child(address).child(String.valueOf(datePresent.hashCode())).setValue(new Transaction(value,datePresent));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    i++;
                    inboxcursor.moveToNext();
                }
            }

            inboxcursor.close();
            listView.setAdapter(cardArrayAdapter);
            if(cardArrayAdapter.getCount()==0) Toast.makeText(ViewSMS.this,"No messages to show",Toast.LENGTH_LONG).show();
            //for searching
            listView.setTextFilterEnabled(true);
        }

    }

    private String checkForDate(String body) throws IOException {
        Pattern date1 = Pattern.compile("[0-9]{1,2}[a-z]*[\\s](Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|SEPT|OCT|NOV|DEC)");
        Pattern date2 = Pattern.compile("[0-9]{1,2}[-](Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|SEPT|OCT|NOV|DEC)[-][0-9]{2}]");
        Pattern date3 = Pattern.compile("[0-9]{1,2}[/][0-9]{1,2}[/][0-9]{2,4}");
        Pattern date4 = Pattern.compile("[0-9]{1,2}[-][0-9]{1,2}[-][0-9]{2,4}");
        String date = "";
        Parser parser=new Parser();
        List<LocalDateModel> dates=parser.parse(body);
        if(dates.size()>0) {
            return dates.get(0).getDateTimeString();
        }
        Matcher matcher1 = date1.matcher(body);
        Matcher matcher2 = date2.matcher(body);
        Matcher matcher3 = date3.matcher(body);
        Matcher matcher4 = date4.matcher(body);

        if(matcher1.find()) return   matcher1.group();
        if(matcher2.find()) return   matcher2.group();
        if(matcher3.find()) return   matcher3.group();
        if(matcher4.find()) return   matcher4.group();

        return date;
    }


    @AfterPermissionGranted(RC_PERM_SMS)
    private void requiresReadSMS() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            showSMS(0);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "This app needs access to read SMS",RC_PERM_SMS
                    , perms);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }
}