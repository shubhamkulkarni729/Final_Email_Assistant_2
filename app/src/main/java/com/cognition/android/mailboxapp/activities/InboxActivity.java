package com.cognition.android.mailboxapp.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cognition.android.mailboxapp.FirebaseMessage;
import com.cognition.android.mailboxapp.MyService;
import com.cognition.android.mailboxapp.R;
import com.cognition.android.mailboxapp.ViewSMS;
import com.cognition.android.mailboxapp.activity_swipe;
import com.cognition.android.mailboxapp.models.Message;
import com.cognition.android.mailboxapp.models.Message_Table;
import com.cognition.android.mailboxapp.settings;
import com.cognition.android.mailboxapp.smart_insights;
import com.cognition.android.mailboxapp.utils.EndlessRecyclerViewScrollListener;
import com.cognition.android.mailboxapp.utils.MessagesAdapter;
import com.cognition.android.mailboxapp.utils.Utils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;
import static com.cognition.android.mailboxapp.activities.MainActivity.REQUEST_AUTHORIZATION;
import static com.cognition.android.mailboxapp.activities.MainActivity.SCOPES;

public class InboxActivity extends AppCompatActivity {

    CoordinatorLayout lytParent;
    SwipeRefreshLayout refreshMessages;
    RecyclerView listMessages;
    List<Message> messageList;
    MessagesAdapter messagesAdapter;
    GoogleAccountCredential mCredential;
    Gmail mService;
    SharedPreferences sharedPref;
    Utils mUtils;
    //String pageToken = null;
    boolean isFetching = false;
    FirebaseDatabase database;
    DatabaseReference myRef;
    List<String> emails = new ArrayList<>();

    //nav bar
    Toolbar toolbar;
    TextView textViewEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        Intent intial = new Intent(InboxActivity.this, MyService.class);
        startService(intial);

        sharedPref = InboxActivity.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);

        Log.d("FLOW", "InboxActivity");
        FloatingActionButton refresh = findViewById(R.id.btn_refresh_mailbox_view);
        refresh.setOnClickListener(v -> {
            Toast.makeText(InboxActivity.this,"Refreshing... Please wait",Toast.LENGTH_LONG).show();
            Intent service = new Intent(InboxActivity.this, MyService.class);
            stopService(service);
            startService(service);
        });

        //nav bar
        toolbar = findViewById(R.id.toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.openNavDrawer, R.string.closeNavDrawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);
        textViewEmail = (TextView)header.findViewById(R.id.nav_header_email);
        textViewEmail.setText(sharedPref.getString(PREF_ACCOUNT_NAME,"Account Name"));

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId())
            {
                case R.id.swipe_view:
                    Intent intent = new Intent(InboxActivity.this, activity_swipe.class);
                    startActivity(intent);
                    break;

                case R.id.mailbox:
                    break;

                case R.id.nav_all_mails:
                    getMessagesFromDB("All");
                    break;

                case R.id.nav_educational_mails:
                    getMessagesFromDB("Education");
                    break;

                case R.id.nav_financial_mails:
                    getMessagesFromDB("Finance");
                    break;

                case R.id.nav_organizational_mails:
                    getMessagesFromDB("Organization");
                    break;

                case R.id.nav_job_mails:
                    getMessagesFromDB("Jobs");
                    break;

                case R.id.nav_offer_mails:
                    getMessagesFromDB("Offers");
                    break;

                case R.id.nav_other_mails:
                    getMessagesFromDB("Other");
                    break;

                case R.id.insight_parent:
                    intent = new Intent(InboxActivity.this, smart_insights.class);
                    startActivity(intent);
                    break;

                case R.id.nav_settings:
                    intent = new Intent(InboxActivity.this, settings.class);
                    startActivity(intent);
                    break;

                case R.id.nav_social_mails:
                    getMessagesFromDB("Social");
                    break;

                case R.id.nav_primary_mails:
                    getMessagesFromDB("Primary");
                    break;

                case R.id.nav_urgent:
                    getMessagesFromDB("Urgent");
                    break;

                case R.id.events:
                    intent = new Intent(InboxActivity.this, EventsActivity.class);
                    startActivity(intent);
                    break;

                case R.id.sms:
                    Toast.makeText(InboxActivity.this,"Loading... Please wait",Toast.LENGTH_SHORT).show();
                    intent = new Intent(InboxActivity.this, ViewSMS.class);
                    startActivity(intent);
                    break;
                case R.id.compose_email:
                    intent = new Intent(InboxActivity.this, ComposeActivity.class);
                    startActivity(intent);
                    break;

            }
            return true;
        });

        navigationView.setCheckedItem(R.id.nav_all_mails);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mService = null;
        mUtils = new Utils(InboxActivity.this);

        String accountName = sharedPref.getString(PREF_ACCOUNT_NAME, null);

        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("MailBox App")
                    .build();

        } else {
            startActivity(new Intent(InboxActivity.this, MainActivity.class));
            ActivityCompat.finishAffinity(InboxActivity.this);
        }

        messageList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(InboxActivity.this, messageList);

        initViews();
        getMessagesFromDB("All");
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

        if (requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == RESULT_OK) {
                if (!isFetching && mUtils.isDeviceOnline()) {
                    //getMessagesFromDB("All");
                } else
                    mUtils.showSnackbar(lytParent, getString(R.string.device_is_offline));
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(InboxActivity.this);
                builder.setMessage(R.string.app_requires_auth);
                builder.setPositiveButton(getString(android.R.string.cancel), (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.finishAffinity(InboxActivity.this);
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
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
     * Initialize the views
     */
    private void initViews() {

        lytParent = findViewById(R.id.lytParent);
        toolbar = findViewById(R.id.toolbar);
        refreshMessages = findViewById(R.id.refreshMessages);
        listMessages = findViewById(R.id.listMessages);
        //fabCompose = findViewById(R.id.fabCompose);
        //toolbar.inflateMenu(R.menu.menu_inbox);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = findViewById(R.id.action_search);
        searchView.setQueryHint(getString(R.string.search));
        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getComponentName()) : null);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                messagesAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                messagesAdapter.getFilter().filter(newText);
                return true;
            }
        });

        refreshMessages.setRefreshing(true);
        refreshMessages.setColorSchemeResources(R.color.colorPrimary);
        refreshMessages.setOnRefreshListener(() -> {
            if (!isFetching && mUtils.isDeviceOnline()) {
                Intent intent = new Intent(InboxActivity.this, MyService.class);
                startService(intent);
                getMessagesFromDB("All");
            } else
                mUtils.showSnackbar(lytParent, getString(R.string.device_is_offline));
        });

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(InboxActivity.this);
        listMessages.setLayoutManager(mLayoutManager);
        listMessages.setItemAnimator(new DefaultItemAnimator());
        listMessages.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (mUtils.isDeviceOnline())
                {
                    //new GetEmailsTask(false).execute();
                }
            }
        });
        listMessages.setAdapter(messagesAdapter);

        /*fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InboxActivity.this, ComposeActivity.class));
            }
        });*/
    }

    /**
     * Get cached emails
     */
    private void getMessagesFromDB(String category) {

        InboxActivity.this.messageList.clear();

        database = FirebaseDatabase.getInstance();
        String name = sharedPref.getString(PREF_ACCOUNT_NAME, null);

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
                    InboxActivity.this.messageList.clear();
                    ArrayList<DataSnapshot> reverse = new ArrayList<>();

                    for(DataSnapshot msg: dataSnapshot.getChildren()) {
                        reverse.add(msg);
                    }

                    for(int i = reverse.size()-1; i>=0; i--) {
                        FirebaseMessage fMessage = reverse.get(i).getValue(FirebaseMessage.class);
                        InboxActivity.this.messageList.add(SQLite.select().from(Message.class).where(Message_Table.message_id.eq(String.valueOf(fMessage.getId()))).querySingle());
                    }
                    InboxActivity.this.messagesAdapter.notifyDataSetChanged();
                }
                else
                {
                    InboxActivity.this.messageList.clear();

                    ArrayList<DataSnapshot> reverse = new ArrayList<>();
                    for(DataSnapshot msg: dataSnapshot.getChildren()) {
                        FirebaseMessage fm = msg.getValue(FirebaseMessage.class);

                        if(fm.getCategory().contains(category)){
                            reverse.add(msg);
                        }
                    }

                    for(int i = reverse.size()-1; i>=0; i--) {
                        FirebaseMessage fMessage = reverse.get(i).getValue(FirebaseMessage.class);
                        InboxActivity.this.messageList.add(SQLite.select().from(Message.class).where(Message_Table.message_id.eq(String.valueOf(fMessage.getId()))).querySingle());
                    }
                    InboxActivity.this.messagesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        InboxActivity.this.messagesAdapter.notifyDataSetChanged();

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

        if (mUtils.isDeviceOnline()) {
            //new GetEmailsTask(true).execute();
        }
        else
            mUtils.showSnackbar(lytParent, getString(R.string.device_is_offline));

        InboxActivity.this.refreshMessages.setRefreshing(false);
    }

    /*

     void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                InboxActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetEmailsTask extends AsyncTask<Void, Void, List<Message>> {

        private int itemCount = 0;
        private boolean clear;
        private Exception mLastError = null;

        GetEmailsTask(boolean clear) {
            this.clear = clear;
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            isFetching = true;
            List<Message> messageListReceived = null;

            if (clear) {
                //Delete.table(Message.class);
                InboxActivity.this.pageToken = null;
            }

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InboxActivity.this.refreshMessages.setRefreshing(true);
                    }
                });

                String user = "me";
                String query = "in:inbox";
                ListMessagesResponse messageResponse = mService.users().messages().list(user).setQ(query).setMaxResults(100L).setPageToken(InboxActivity.this.pageToken).execute();
                InboxActivity.this.pageToken = messageResponse.getNextPageToken();

                messageListReceived = new ArrayList<>();
                List<com.google.api.services.gmail.model.Message> receivedMessages = messageResponse.getMessages();
                for (com.google.api.services.gmail.model.Message message : receivedMessages) {
                    com.google.api.services.gmail.model.Message actualMessage = mService.users().messages().get(user, message.getId()).execute();


                    Map<String, String> headers = new HashMap<>();
                    for (MessagePartHeader messagePartHeader : actualMessage.getPayload().getHeaders())
                        headers.put(
                                messagePartHeader.getName(), messagePartHeader.getValue()
                        );

                    MessagePart msg = actualMessage.getPayload();
                    String body = StringUtils.newStringUtf8(Base64.decodeBase64(msg.getBody().getData()));

                    Message newMessage = new Message(
                            actualMessage.getId(),
                            actualMessage.getLabelIds(),
                            actualMessage.getSnippet(),
                            actualMessage.getPayload().getMimeType(),
                            headers,
                            actualMessage.getPayload().getParts(),
                            actualMessage.getInternalDate(),
                            InboxActivity.this.mUtils.getRandomMaterialColor(),
                            actualMessage.getPayload(),
                            body);

                    try {
                        JSONObject parentPart = new JSONObject(newMessage.getParentPartJson());

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

                            //Log.d("DOCUMENT TEXT IS::",document.body());
                            newMessage.setBody(document.text());
                            //newMessage.setSummary(Summary.summarize(document.text(),newMessage.getSubject(),getApplicationContext()));
                            //data = data.replaceAll("\\<.*?>", "");

                        } else {
                            JSONArray partsArray = new JSONArray(newMessage.getPartsJson());

                            String[] result = getData(partsArray);
                            if (result[0] != null && result[1] != null) {
                                //(data,mimeType,encoding)

                                result[1] = result[1].replaceAll("\\<.*?>", "");
                                //System.out.println("THE DATA IS::::" + result[1]);

                                Document document = Jsoup.parse(result[1]);
                                //Log.d("DOCUMENT TEXT IS::", document.text());
                                newMessage.setBody(document.text());
                                //newMessage.setSummary(Summary.summarize(document.text(),newMessage.getSubject(),getApplicationContext()));
                                //Log.d(MainActivity.TAG, result[0]);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("messages");
                    FirebaseMessage firebaseMessage = new FirebaseMessage(newMessage);
                    myRef.child(actualMessage.getId()).setValue(firebaseMessage);

                    if(newMessage.exists())
                        newMessage.update();
                    else
                        newMessage.save();

                    messageListReceived.add(newMessage);
                    itemCount++;
                }
            } catch (Exception e) {
                Log.w(TAG, e);
                mLastError = e;
                cancel(true);
            }

            return messageListReceived;
        }

        @Override
        protected void onPostExecute(List<Message> output) {
            isFetching = false;

            if (output != null && output.size() != 0) {
                if (clear) {
                    InboxActivity.this.messageList.clear();
                    InboxActivity.this.messageList.addAll(output);
                    InboxActivity.this.messagesAdapter.notifyDataSetChanged();
                } else {
                    int listSize = InboxActivity.this.messageList.size();
                    InboxActivity.this.messageList.addAll(output);
                    InboxActivity.this.messagesAdapter.notifyItemRangeInserted(listSize, itemCount);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InboxActivity.this.refreshMessages.setRefreshing(false);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InboxActivity.this.refreshMessages.setRefreshing(false);
                    }
                });
                InboxActivity.this.mUtils.showSnackbar(lytParent, getString(R.string.fetch_failed));
            }
        }

        @Override
        protected void onCancelled() {
            isFetching = false;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InboxActivity.this.refreshMessages.setRefreshing(false);
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

     */

    private void createNotificationChannel(String subject){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_all_mails)
                        .setContentTitle("You have received email from one of your priority list")
                        .setContentText(subject);

        Intent notificationIntent = new Intent(this, InboxActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}