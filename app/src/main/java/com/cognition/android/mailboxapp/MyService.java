package com.cognition.android.mailboxapp;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cognition.android.mailboxapp.models.Message;
import com.cognition.android.mailboxapp.models.Message_Table;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cognition.android.mailboxapp.activities.MainActivity.ORGANIZATION_NAME;
import static com.cognition.android.mailboxapp.activities.MainActivity.PREFERRED_EMAIL_ADDRESSES;
import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;
import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_CATEGORIES;
import static com.cognition.android.mailboxapp.activities.MainActivity.SCOPES;
import static com.cognition.android.mailboxapp.activities.MainActivity.TAG;

public class MyService extends Service {

    boolean isRunning;
    List<com.cognition.android.mailboxapp.models.Message> messageListReceived = null;
    GoogleAccountCredential mCredential;
    Gmail mService;
    com.cognition.android.mailboxapp.utils.Utils mUtils;
    SharedPreferences sharedPref;
    List<String> ID;
    private static final String NOTIFIED_EMAILS_IDS = "notifiedEmailsIds";

    boolean appInForeground = false;

    ArrayList<String>notifiedMails ;
    ArrayList<String>notifiedCategories;
    String preferredCategories;
    String preferredEmailAddresses;
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference myRefToCheckIfExists;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //Don't write anything here.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mService = null;
        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        mUtils = new com.cognition.android.mailboxapp.utils.Utils(getApplicationContext());
        isRunning = true;
        ID = new ArrayList<>();
        String accountName = sharedPref.getString(PREF_ACCOUNT_NAME, null);

        String notifiedEmailsString = sharedPref.getString(NOTIFIED_EMAILS_IDS,"");
        Log.d(TAG,notifiedEmailsString);
        String[] notifiedEmailsArray = notifiedEmailsString.split(",");
        notifiedMails = new ArrayList<>(Arrays.asList(notifiedEmailsArray));

        preferredCategories = sharedPref.getString(PREF_CATEGORIES,"");
        preferredEmailAddresses = sharedPref.getString(PREFERRED_EMAIL_ADDRESSES,"");

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("messages");

        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("MailBox App")
                    .build();

            myRefToCheckIfExists = database.getReference("messages/"+sharedPref.getString(PREF_ACCOUNT_NAME,null).hashCode());
        }
        //Dont write anything here.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        final boolean[] urgent = {true};
        //Write mail fetching and uploading for processing code in run(). Put thread.sleep as 10 min
        new Thread(() -> {
            //Your logic that service will perform will be placed here
            //In this example we are just looping and waits for 1000 milliseconds in each loop.
            while(true) {
                try {
                    if(isNetworkConnected()) {
                        String user = "me";
                        //only unread mails. Not dealing with mail IDs. Please don't add mails whose IDs are already present in DB table.
                        String query = "in:inbox";

                        ListMessagesResponse messageResponse = mService.users().messages().list(user).setQ(query).setMaxResults(20L).execute();
                        com.google.api.services.gmail.model.Profile profile =  mService.users().getProfile("me").execute();
                        String name = profile.getEmailAddress();

                        messageListReceived = new ArrayList<>();
                        final int[] flag = new int[1];
                        List<com.google.api.services.gmail.model.Message> receivedMessages = messageResponse.getMessages();
                        for (com.google.api.services.gmail.model.Message message : receivedMessages) {
                            com.google.api.services.gmail.model.Message actualMessage = mService.users().messages().get(user, message.getId()).execute();

                            //checking if message already in sqlite.
                            Message mMessage = SQLite.select().from(Message.class).where(Message_Table.message_id.eq(actualMessage.getId())).querySingle();

                            if(mMessage!=null)      //msg in sqlite. check if notified and in firebase. If not notified then notify. if not in firebase then add.
                            {
                                myRefToCheckIfExists.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(!dataSnapshot.hasChild(mMessage.getMessage_id())) {
                                            boolean result = uploadToFirebase(mMessage,name);       //upload
                                        }

                                        else                //message in the firebase. Check for notifications
                                        {
                                            FirebaseMessage fm = dataSnapshot.child(mMessage.getMessage_id()).getValue(FirebaseMessage.class);
                                            if(!notifiedMails.contains(fm.getId())) {
                                                String[] messageCategories = fm.getCategory().split("/");

                                                for (String category : messageCategories) {

                                                    if(fm.getCategory().contains("Organization"))
                                                    {
                                                        createNotificationChannelCategory("Organization emails",fm.getFrom(),fm.getSubject());
                                                        notifiedMails.add(fm.getId());
                                                        savePreferences();
                                                        break;
                                                    }

                                                    if(fm.getCategory().contains("Urgent"))
                                                    {
                                                        createNotificationChannelCategory("Urgent emails",fm.getFrom(),fm.getSubject());
                                                        notifiedMails.add(fm.getId());
                                                        break;
                                                    }

                                                    if (preferredCategories.contains(category.toLowerCase()) && !notifiedMails.contains(fm.getId())) {
                                                        createNotificationChannelCategory("Preferred category email",category, fm.getFrom());
                                                        notifiedMails.add(fm.getId());
                                                        savePreferences();
                                                        break;
                                                    }

                                                }

                                                if(preferredEmailAddresses.contains(fm.getFrom()) && !notifiedMails.contains(fm.getId())){
                                                    createNotificationChannelCategory("Preferred sender email", fm.getFrom(),fm.getSubject());
                                                    notifiedMails.add(fm.getId());
                                                    savePreferences();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else        //Message not in sqlite
                            {
                                //save to sqlite first.
                                Message newMessage = saveToLocalDB(actualMessage);

                                //upload to Firebase
                                boolean result = uploadToFirebase(newMessage,name);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
                if(isRunning){
                    Log.d(TAG, "Email Service running");
                }

                if(notifiedMails.size()>20) {
                    notifiedMails = (ArrayList<String>) notifiedMails.subList(notifiedMails.size() - 20, notifiedMails.size());
                    savePreferences();
                }
                try {
                    Thread.sleep(1200000);      //20 mins thread sleep
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,"Service running");
            }

        }).start();
        return Service.START_STICKY;
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

    boolean isUrgent(String text)
    {
        double threshold = 0.20;
        List<String> keywords=Arrays.asList("urgent","important","revert","last date","need","you need","you have","kindly","please","pls","asap","as soon as possible","verific","convey","meeting","today","tomorrow","compulsory","require","apply","register");

        int cnt=0;
        text=text.toLowerCase();

        for(String temp : keywords)
        {
            if(text.contains(temp))
                cnt++;
        }

        int totalWords = text.split("\\s+").length;
        double result = cnt/totalWords;
        return result>=threshold ;
    }

    private boolean uploadToFirebase(Message newMessage, String accountName) {

        FirebaseMessage firebaseMessage = new FirebaseMessage(newMessage);

        for (String label : newMessage.getLabels()) {
            if (label.contains("SOCIAL")) {
                firebaseMessage.setCategory("Social");
            } else if (label.contains("CATEGORY_PERSONAL")) {
                firebaseMessage.setCategory("Primary");
            } else if (label.contains("CATEGORY_PROMOTIONS")) {
                firebaseMessage.setCategory("Offers");
            } else if (label.contains("CATEGORY_FORUMS")) {
                firebaseMessage.setCategory("Forums");
            }
        }

        if (isUrgent(firebaseMessage.getBody())) {
            firebaseMessage.setCategory(firebaseMessage.getCategory() + "/Urgent");
        }

            String organizationName = sharedPref.getString(ORGANIZATION_NAME, "null");
            if (newMessage.getFrom().contains(organizationName)) {
                firebaseMessage.setCategory(firebaseMessage.getCategory() + "/Organization");
            }

            if (!firebaseMessage.getCategory().contains("Primary")) {
                if (newMessage.getFrom().contains("gmail") || newMessage.getFrom().contains("yahoo")) {
                    firebaseMessage.setCategory(firebaseMessage.getCategory() + "/Primary");
                }
            }

            myRef.child(String.valueOf(accountName.hashCode())).child(newMessage.getMessage_id()).setValue(firebaseMessage);
        return true;
    }

    private Message saveToLocalDB(com.google.api.services.gmail.model.Message actualMessage) throws JSONException {

        Map<String, String> headers = new HashMap<>();
        for (MessagePartHeader messagePartHeader : actualMessage.getPayload().getHeaders())
            headers.put(
                    messagePartHeader.getName(), messagePartHeader.getValue()
            );

        String body = "test";
        Message newMessage = new Message(
                actualMessage.getId(),
                actualMessage.getLabelIds(),
                actualMessage.getSnippet(),
                actualMessage.getPayload().getMimeType(),
                headers,
                actualMessage.getPayload().getParts(),
                actualMessage.getInternalDate(),
                mUtils.getRandomMaterialColor(),
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

                newMessage.setBody(document.text());
            } else {
                JSONArray partsArray = new JSONArray(newMessage.getPartsJson());

                String[] result = getData(partsArray);
                if (result[0] != null && result[1] != null) {
                    result[1] = result[1].replaceAll("<.*?>", "");

                    Document document = Jsoup.parse(result[1]);
                    newMessage.setBody(document.text());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        newMessage.save();
        return newMessage;

    }

    private boolean applicationInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> services = activityManager.getRunningAppProcesses();
        boolean isActivityFound = false;

        if (services.get(0).processName
                .equalsIgnoreCase(getPackageName()) && services.get(0).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            isActivityFound = true;
        }

        return isActivityFound;
    }

    private void createNotificationChannelCategory(String title, String contextText, String subText){
        if(applicationInForeground()) return;       //if application is open then do not give notifs


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this,"M_CH_ID")
                        .setSmallIcon(R.drawable.ic_all_mails)
                        .setContentTitle(title)
                        .setContentText(contextText)
                        .setSubText(subText);

        Intent notificationIntent = new Intent(this, activity_swipe.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int)(Math.random()*((1001))), builder.build());

    }

    @Override
    public void onDestroy() {
        isRunning = false;
        Log.i(TAG, "Service onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        Log.d(TAG,"Service started again");

        super.onTaskRemoved(rootIntent);
    }

    private void savePreferences(){

        StringBuilder sb = new StringBuilder();
        for (String s : notifiedMails)
        {
            sb.append(s);
            sb.append(",");
        }
        String notifiedEmailsString = sb.toString();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(NOTIFIED_EMAILS_IDS, notifiedEmailsString);
        editor.apply();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}