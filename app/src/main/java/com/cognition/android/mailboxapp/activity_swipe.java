package com.cognition.android.mailboxapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cognition.android.mailboxapp.activities.ComposeActivity;
import com.cognition.android.mailboxapp.activities.EmailActivity;
import com.cognition.android.mailboxapp.activities.EventsActivity;
import com.cognition.android.mailboxapp.activities.InboxActivity;
import com.cognition.android.mailboxapp.activities.MainActivity;
import com.cognition.android.mailboxapp.activities.OrganizationActivity;
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
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;
import static com.cognition.android.mailboxapp.activities.MainActivity.REQUEST_AUTHORIZATION;
import static com.cognition.android.mailboxapp.activities.MainActivity.SCOPES;

public class activity_swipe extends AppCompatActivity {

    //List<Message> messageList;
    //MessagesAdapter messagesAdapter;
    GoogleAccountCredential mCredential;
    Gmail mService;
    SharedPreferences sharedPref;
    com.cognition.android.mailboxapp.utils.Utils mUtils;
    //boolean isFetching = false;
    FrameLayout lytParent;
    //SwipeRefreshLayout refreshMessages;
    //List<Message> messages;
    SwipePlaceHolderView mSwipeView;
    DatabaseReference myRef;
    //nav bar
    Toolbar toolbar;
    TextView textViewEmail;
    public static final String PREF_VIEW_TYPE = "viewType";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        Log.d("FLOW", "SwipeActivity");

        FloatingActionButton refresh = findViewById(R.id.refresh_but);
        refresh.setOnClickListener(v -> {
            Toast.makeText(activity_swipe.this,"Refreshing... Please wait",Toast.LENGTH_LONG).show();
            Intent service = new Intent(activity_swipe.this, MyService.class);
            stopService(service);
            startService(service);
        });

        //nav bar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Inbox");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        sharedPref = activity_swipe.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);

        View header = navigationView.getHeaderView(0);
        textViewEmail = header.findViewById(R.id.nav_header_email);
        textViewEmail.setText(sharedPref.getString(PREF_ACCOUNT_NAME,"Account Name"));

        String accountName = sharedPref.getString(PREF_ACCOUNT_NAME, null);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId())
            {
                case R.id.swipe_view:
                    break;

                case R.id.mailbox:
                    Intent intent = new Intent(activity_swipe.this, InboxActivity.class);
                    startActivity(intent);
                    break;

                case R.id.nav_all_mails:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                        addCards("All");
                    break;

                case R.id.nav_educational_mails:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                    addCards("Education");
                    break;

                case R.id.nav_other_mails:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                    addCards("Other");
                    break;

                case R.id.insight_parent:
                    intent = new Intent(activity_swipe.this, smart_insights.class);
                    startActivity(intent);
                    break;

                case R.id.nav_financial_mails:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                    addCards("Finance");
                    break;

                case R.id.nav_organizational_mails:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                    addCards("Organization");
                    break;

                case R.id.nav_job_mails:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                    addCards("Jobs");
                    break;

                case R.id.nav_offer_mails:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                    addCards("Offers");
                    break;

                case R.id.nav_settings:
                    intent = new Intent(activity_swipe.this, settings.class);
                    startActivity(intent);
                    break;

                case R.id.nav_social_mails:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                    addCards("Social");
                    break;

                case R.id.nav_primary_mails:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                    addCards("Primary");
                    break;

                case R.id.nav_urgent:
                    if(mSwipeView!=null) {
                        mSwipeView.removeAllViews();
                    }
                    addCards("Urgent");
                    break;

                case R.id.events:
                    intent = new Intent(activity_swipe.this, EventsActivity.class);
                    startActivity(intent);
                    break;

                case R.id.sms:
                    Toast.makeText(activity_swipe.this,"Loading... Please wait",Toast.LENGTH_SHORT).show();
                    intent = new Intent(activity_swipe.this, ViewSMS.class);
                    startActivity(intent);
                    break;

                case R.id.compose_email:
                    intent = new Intent(activity_swipe.this, ComposeActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.openNavDrawer, R.string.closeNavDrawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(R.id.nav_all_mails);

        //refreshMessages = findViewById(R.id.refreshMessages);
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mService = null;
        sharedPref = activity_swipe.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        mUtils = new com.cognition.android.mailboxapp.utils.Utils(activity_swipe.this);

        //String accountName = sharedPref.getString(PREF_ACCOUNT_NAME, null);

        if (accountName != null) {

            mCredential.setSelectedAccountName(accountName);
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("MailBox App")
                    .build();

        } else {
            startActivity(new Intent(activity_swipe.this, MainActivity.class));
            ActivityCompat.finishAffinity(activity_swipe.this);
        }

        //messageList = new ArrayList<>();
        //messagesAdapter = new MessagesAdapter(activity_swipe.this, messageList);
        //getMessagesFromDB();

        lytParent = findViewById(R.id.lytParent);
        /*for(com.cognition.android.mailboxapp.Profile profile : Utils.loadProfiles(this.getApplicationContext())){
            mSwipeView.addView(new TinderCard(mContext, profile, mSwipeView));
        }
        if(done==true) {
            for (String id : ID) {
                Message message = SQLite.select().from(Message.class).where(Message_Table.id.eq(Integer.valueOf(id))).querySingle();

                Profile profile = new Profile();
                profile.setId(message.getId());
                profile.setSubject(message.getSubject());
                profile.setMessage(message.getSnippet());
                profile.setSender(message.getFrom());

                mSwipeView.addView(new TinderCard(mContext, profile, mSwipeView));
            }
        }
        WorkManager.initialize(
                getApplicationContext(),
                new Configuration.Builder()
                .setExecutor(Executors.newFixedThreadPool(8))
                .build()
        );
        */

        addCards("All");
    }

    void addCards(String category)
    {
        Button read = findViewById(R.id.read_but);
        Button delete = findViewById(R.id.delete_but);
        Button detail = findViewById(R.id.detail_but);
        mSwipeView = findViewById(R.id.swipeView);

        mSwipeView.getBuilder()
                .setDisplayViewCount(3)
                .setSwipeDecor(new SwipeDecor()
                        .setPaddingTop(20)
                        .setRelativeScale(0.01f)
                        .setSwipeInMsgLayoutId(R.layout.tinder_swipe_in_msg_view)
                        .setSwipeOutMsgLayoutId(R.layout.tinder_swipe_out_msg_view));

        //List<Message> messages = SQLite.select().from(Message.class).queryList();

        List<String> emails = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String name = sharedPref.getString(PREF_ACCOUNT_NAME, null);

        HashMap<String, String> summaries = new HashMap<>();
        myRef = database.getReference("Summary/"+name.hashCode());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren())
                {
                    HashMap sum =(HashMap) d.getValue();
                    summaries.put(d.getKey(), (String) sum.get("summary"));

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef = database.getReference("Preferences/userId");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                emails.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    emails.add(d.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef = database.getReference("messages/" + (name.hashCode()));
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(category.equals("All"))
                {
                    ArrayList<DataSnapshot> reverse = new ArrayList<>();

                    for(DataSnapshot msg: dataSnapshot.getChildren()) {
                        reverse.add(msg);
                    }

                    for(int i = reverse.size()-1; i>=0; i--) {

                        FirebaseMessage fMessage = reverse.get(i).getValue(FirebaseMessage.class);
                        Message message = SQLite.select().from(Message.class).where(Message_Table.message_id.eq(String.valueOf(fMessage.getId()))).querySingle();

                            if (!message.isRead()) {
                                Profile profile = new Profile();
                                profile.setId(String.valueOf(message.getId()));
                                profile.setSubject(message.getSubject());
                                profile.setSender(message.getFrom());
                                profile.setCategory(fMessage.getCategory());
                                if(summaries.containsKey(message.getMessage_id()))
                                {
                                    profile.setMessage(summaries.get(message.getMessage_id()));
                                }
                                else
                                    profile.setMessage(message.getSnippet());


                                mSwipeView.addView(new TinderCard(getApplication(), profile, mSwipeView));
                            }
                        }
                }
                else
                {
                    ArrayList<DataSnapshot> reverse = new ArrayList<>();

                    for(DataSnapshot msg: dataSnapshot.getChildren()) {
                        reverse.add(msg);
                    }

                    for(int i = reverse.size()-1; i>=0; i--) {
                        FirebaseMessage fMessage = reverse.get(i).getValue(FirebaseMessage.class);
                        Message message = SQLite.select().from(Message.class).where(Message_Table.message_id.eq(String.valueOf(fMessage.getId()))).querySingle();

                        if(fMessage.getCategory().contains(category)) {
                            if (!message.isRead()) {
                                Profile profile = new Profile();
                                profile.setId(String.valueOf(message.getId()));
                                profile.setSubject(message.getSubject());
                                profile.setSender(message.getFrom());
                                profile.setCategory(fMessage.getCategory());
                                if(summaries.containsKey(fMessage.getId()))
                                {
                                    profile.setMessage(summaries.get(fMessage.getId()));
                                }
                                else
                                    profile.setMessage(message.getSnippet());

                                mSwipeView.addView(new TinderCard(getApplication(), profile, mSwipeView));
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef = database.getReference("messages/" + (name.hashCode()));
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren())
                {
                    FirebaseMessage fm = d.getValue(FirebaseMessage.class);
                    for(String e: emails)
                    {
                        if(e.equals(fm.getFrom()))
                        {
                            createNotificationChannel(fm.getFrom());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*
        for(Message message: messages) {
            if(!message.isRead()) {
                Profile profile = new Profile();
                Message database_msg = SQLite.select().from(Message.class).where(Message_Table.message_id.eq(String.valueOf(message.getId()))).querySingle();
                profile.setId(String.valueOf(message.getId()));
                profile.setSubject(database_msg.getSubject());
                profile.setSender(database_msg.getFrom());
                profile.setMessage(database_msg.getSummary());
                mSwipeView.addView(new TinderCard(getApplication(), profile, mSwipeView));
            }
        }
        */
        read.setOnClickListener(v -> mSwipeView.doSwipe(true));

        delete.setOnClickListener(v -> mSwipeView.doSwipe(false));

        //myRef = database.getReference("Read");
        detail.setOnClickListener(v -> {
            List<Object> list = mSwipeView.getAllResolvers();
            Object obj = list.get(0);
            TinderCard card = (TinderCard) obj;

            Intent intent = new Intent(activity_swipe.this, EmailActivity.class);
            intent.putExtra("messageId",Integer.valueOf(card.mProfile.getId()));
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_AUTHORIZATION) {
                if (resultCode == RESULT_OK) {
                    if (mUtils.isDeviceOnline()) {
                        //addCards("All");
                    } else
                        mUtils.showSnackbar(lytParent, getString(R.string.device_is_offline));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity_swipe.this);
                    builder.setMessage(R.string.app_requires_auth);
                    builder.setPositiveButton(getString(android.R.string.cancel), (dialog, which) -> {
                        dialog.dismiss();
                        ActivityCompat.finishAffinity(activity_swipe.this);
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    private void createNotificationChannel(String subject){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_all_mails)
                        .setContentTitle("You have received email from one of your priority list")
                        .setContentText(subject);

        Intent notificationIntent = new Intent(this, activity_swipe.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    /*
    private void getMessagesFromDB() {
        activity_swipe.this.refreshMessages.setRefreshing(true);
        activity_swipe.this.messageList.clear();
        activity_swipe.this.messageList.addAll(SQLite.select().from(Message.class).queryList());
        activity_swipe.this.refreshMessages.setRefreshing(false);

        if (mUtils.isDeviceOnline())
        {
            //new GetEmailsTask(true).execute();
        }
        else
            mUtils.showSnackbar(lytParent, getString(R.string.device_is_offline));
    }

    private String[] getData(JSONArray parts) throws JSONException {
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = new JSONObject(parts.getString(i));
            if (part.has("parts"))
                return getData(new JSONArray(part.getString("parts")));
            else {
                if (part.getString("mimeType").equals("text/html"))
                    return new String[]{
                            part.getString("mimeType"),
                            new String(
                                    Base64.decodeBase64(part.getJSONObject("body").getString("data")),
                                    StandardCharsets.UTF_8
                            )
                    };
            }
        }
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = new JSONObject(parts.getString(i));
            if (part.getString("mimeType").equals("text/plain"))
                return new String[]{
                        part.getString("mimeType"),
                        new String(
                                Base64.decodeBase64(part.getJSONObject("body").getString("data")),
                                StandardCharsets.UTF_8
                        )
                };
        }
        return new String[]{null, null};
    }


    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                activity_swipe.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetEmailsTask extends AsyncTask<Void, Void, List<Message>> {

        private boolean clear;
        private int itemCount = 0;
        private Exception mLastError = null;

        GetEmailsTask(boolean clear) {
            this.clear = clear;
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            isFetching = true;

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity_swipe.this.refreshMessages.setRefreshing(true);
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<Message> messages = SQLite.select().from(Message.class).queryList();
                        for (Message message : messages) {
                            try {
                                JSONObject parentPart = new JSONObject(message.getParentPartJson());

                                if (parentPart.getJSONObject("body").getInt("size") != 0) {
                                    byte[] dataBytes = Base64.decodeBase64(parentPart.getJSONObject("body").getString("data"));
                                    String data = new String(dataBytes, StandardCharsets.UTF_8);

                                    Document document = Jsoup.parse(data);
                                    Elements el = document.getAllElements();
                                    for (Element e : el) {
                                        Attributes at = e.attributes();
                                        for (Attribute a : at) {
                                            e.removeAttr(a.getKey());
                                        }
                                    }
                                    document.getElementsByTag("style").remove();
                                    document.select("[style]").removeAttr("style");

                                    message.setSummary(Summary.summarize(document.text(), message.getSubject(), getApplicationContext()));
                                    message.update();
                                    itemCount++;
                                } else {
                                    JSONArray partsArray = new JSONArray(message.getPartsJson());

                                    String[] result = getData(partsArray);
                                    if (result[0] != null && result[1] != null) {

                                        result[1] = result[1].replaceAll("<.*?>", "");
                                        //System.out.println("THE DATA IS::::"+result[1]);

                                        Document document = Jsoup.parse(result[1]);
                                        Log.d("DOCUMENT TEXT IS::", document.text());

                                        Log.d(MainActivity.TAG, result[0]);
                                        message.setSummary(Summary.summarize(document.text(), message.getSubject(), getApplicationContext()));
                                        message.update();
                                        itemCount++;
                                    }
                                }
                            } catch (Exception e) {
                                Log.w(MainActivity.TAG, e);
                                e.printStackTrace();
                                cancel(true);
                            }
                        }
                    }
                }).start();



                /*
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("messages");

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        for(DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            FirebaseMessage firebaseMessage = ds.getValue(FirebaseMessage.class);
                            try {
                                firebaseMessage.setSummary(Summary.summarize(firebaseMessage.getBody(),firebaseMessage.getSubject(),getApplicationContext()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                    }
                });

            }catch (Exception e){
                mLastError = e;
                cancel(true);
            }
                return messageList;
        }

        @Override
        protected void onPostExecute(List<Message> output) {
            isFetching = false;

            if (output != null && output.size() != 0) {
                if (clear) {
                    activity_swipe.this.messageList.clear();
                    activity_swipe.this.messageList.addAll(output);
                    activity_swipe.this.messagesAdapter.notifyDataSetChanged();
                    //addCards();
                } else {
                    int listSize = activity_swipe.this.messageList.size();
                    activity_swipe.this.messageList.addAll(output);
                    activity_swipe.this.messagesAdapter.notifyItemRangeInserted(listSize, itemCount);
                    //addCards();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity_swipe.this.refreshMessages.setRefreshing(false);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity_swipe.this.refreshMessages.setRefreshing(false);
                    }
                });
                activity_swipe.this.mUtils.showSnackbar(lytParent, getString(R.string.fetch_failed));
            }
        }

        @Override
        protected void onCancelled() {
            isFetching = false;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity_swipe.this.refreshMessages.setRefreshing(false);
                }
            });

            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mUtils.showSnackbar(lytParent, getString(R.string.an_error_occurred));
                }
            } else {
                mUtils.showSnackbar(lytParent, getString(R.string.an_error_occurred));
            }
        }
    }
    */

}