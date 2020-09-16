package com.cognition.android.mailboxapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cognition.android.mailboxapp.Card;
import com.cognition.android.mailboxapp.CardArrayAdapter;
import com.cognition.android.mailboxapp.R;
import com.cognition.android.mailboxapp.ViewSMS;
import com.cognition.android.mailboxapp.activity_swipe;
import com.cognition.android.mailboxapp.models.Message;
import com.cognition.android.mailboxapp.models.Message_Table;
import com.cognition.android.mailboxapp.settings;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;
import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_CATEGORIES;

public class EventsActivity extends AppCompatActivity {
    CardArrayAdapter cardArrayAdapter;
    ListView listView;
    TextView textViewEmail;
    SharedPreferences sharedPref;
    String preferredCategories;         //String containing all preferred categories
    CompactCalendarView compactCalendarView;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM-yyyy", Locale.getDefault());
    ArrayList<String> date = new ArrayList<>(), subject = new ArrayList<>(), time = new ArrayList<>(), venue = new ArrayList<>(), ids = new ArrayList<>();
    boolean eventsPresent ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        Log.d("FLOW", "EventActivity");
        cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_card);
        eventsPresent = false;
        sharedPref = EventsActivity.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);

        preferredCategories = sharedPref.getString(PREF_CATEGORIES,"");

        /*
        preferredCategories variable contains all the preferred categories.
        When getting events from firebase, get ID of the particular email of that event
        Obtain the category String of that mail from firebase.
        use this code further. Suppose mail category string obtained from firebase for a single mail ID is categoriesFromFirebase

        String[] mailCategoryArray = categoriesFromFirebase.split("/");
        for(String category : mailCategoryArray)
        {
            if(preferredCategories.contains(category.toLowerCase()))
            {
                //This email belongs to the preferred category. Add its card to the view.
                //toLowerCase() function is important. Else wont work.
            }
        }



         */

        listView =  findViewById(R.id.card_listView);
        listView.addHeaderView(new View(this));
        listView.addFooterView(new View(this));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Card card = (Card) parent.getItemAtPosition(position);

            Message msg = SQLite.select().from(Message.class).where(Message_Table.message_id.eq(String.valueOf(card.getId()))).querySingle();
            Intent in = new Intent(EventsActivity.this, EmailActivity.class);
            in.putExtra("messageId", Objects.requireNonNull(msg).getId());
            startActivity(in);

        });

        //nav bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Calendar");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.openNavDrawer, R.string.closeNavDrawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);
        textViewEmail = header.findViewById(R.id.nav_header_email);
        textViewEmail.setText(sharedPref.getString(PREF_ACCOUNT_NAME,"Account Name"));
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId())
            {
                case R.id.swipe_view:
                    Intent intent = new Intent(EventsActivity.this, activity_swipe.class);
                    startActivity(intent);
                    break;

                case R.id.mailbox:
                    intent = new Intent(EventsActivity.this, InboxActivity.class);
                    startActivity(intent);
                    break;

                case R.id.nav_settings:
                    intent = new Intent(EventsActivity.this, settings.class);
                    startActivity(intent);
                    break;

                case R.id.events:
                    intent = new Intent(EventsActivity.this, EventsActivity.class);
                    startActivity(intent);
                    break;

                case R.id.sms:
                    intent = new Intent(EventsActivity.this, ViewSMS.class);
                    startActivity(intent);
                    break;

                case R.id.compose_email:
                    intent = new Intent(EventsActivity.this, ComposeActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        navigationView.setCheckedItem(R.id.events);

        Spinner spinner = findViewById(R.id.spinner);
        List<String> list = new ArrayList<>();
        list.add("Today");
        list.add("Tomorrow");
        list.add("This Week");
        list.add("This Month");
        list.add("This Year");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){

                    case 0:
                        EventsActivity.this.cardArrayAdapter = null;
                        cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_card);
                        EventsActivity.this.cardArrayAdapter.notifyDataSetChanged();

                        int year = Calendar.getInstance().get(Calendar.YEAR);
                        int i;

                        for (i = 0; i<date.size(); i++) {
                            if (date.get(i).contains(String.valueOf(year))) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                Calendar c = Calendar.getInstance();
                                String today = sdf.format(c.getTime());


                                if (date.get(i).equals(today)) {
                                    Log.d("Events", today+"\t"+subject.get(i));

                                    Card card = new Card(time.get(i), venue.get(i), date.get(i), subject.get(i), ids.get(i));
                                    EventsActivity.this.cardArrayAdapter.add(card);
                                }
                            }
                        }
                        listView.setAdapter(cardArrayAdapter);
                        if(cardArrayAdapter.getCount()==0){
                            Toast.makeText(EventsActivity.this,"No events to display",Toast.LENGTH_LONG).show();
                        }
                        break;

                    case 1:
                        EventsActivity.this.cardArrayAdapter = null;
                        cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_card);
                        EventsActivity.this.cardArrayAdapter.notifyDataSetChanged();

                        year = Calendar.getInstance().get(Calendar.YEAR);
                        for (i = 0; i<date.size(); i++) {
                            if (date.get(i).contains(String.valueOf(year))) {
                                int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+1;
                                int month = Calendar.getInstance().get(Calendar.MONTH)+1;

                                String tommorrow = day+"/"+month+"/"+year;

                                if(day > 9 && month>9)
                                    tommorrow = day + "/" + month + "/" + year;
                                else if(day > 9 && month < 10)
                                    tommorrow = day + "/0" + month + "/" + year;
                                else if(day < 10 && month < 10)
                                    tommorrow = "0"+day + "/0" + month + "/" + year;
                                else if(day < 10 && month > 9)
                                    tommorrow = "0"+day + "/" + month + "/" + year;


                                if(date.get(i).equals(tommorrow)) {
                                    Log.d("Events",tommorrow+"\t"+subject.get(i));

                                    Card card = new Card(time.get(i), venue.get(i), date.get(i), subject.get(i), ids.get(i));
                                    EventsActivity.this.cardArrayAdapter.add(card);
                                }
                            }
                        }
                        listView.setAdapter(cardArrayAdapter);
                        if(cardArrayAdapter.getCount()==0){
                            Toast.makeText(EventsActivity.this,"No events to display",Toast.LENGTH_LONG).show();
                        }
                        break;

                    case 2:
                        EventsActivity.this.cardArrayAdapter = null;
                        cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_card);
                        EventsActivity.this.cardArrayAdapter.notifyDataSetChanged();

                        year = Calendar.getInstance().get(Calendar.YEAR);
                        for (i = 0; i<date.size(); i++) {
                            if (date.get(i).contains(String.valueOf(year))) {
                                int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+1;
                                int month = Calendar.getInstance().get(Calendar.MONTH)+1;
                                int week  = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

                                String start;
                                List<String> weekDays = new ArrayList<>();
                                weekDays.clear();

                                for(int j=0;j<7;j++) {
                                    if (day > 9 && month > 9) {
                                        start = day - week + "/" + month + "/" + year;
                                        weekDays.add(start);
                                    } else if (day > 9 && month < 10) {
                                        start = day - week + "/0" + month + "/" + year;
                                        weekDays.add(start);
                                    } else if (day < 10 && month < 10) {
                                        start = "0" + (day - week) + "/0" + month + "/" + year;
                                        weekDays.add(start);
                                    } else if (day < 10 && month > 9) {
                                        start = "0" + (day - week) + "/" + month + "/" + year;
                                        weekDays.add(start);
                                    }
                                    day = day + 1;
                                }

                                if (weekDays.contains(date.get(i))) {
                                    Card card = new Card(time.get(i), venue.get(i), date.get(i), subject.get(i), ids.get(i));
                                    EventsActivity.this.cardArrayAdapter.add(card);
                                }
                            }
                        }
                        EventsActivity.this.cardArrayAdapter.notifyDataSetChanged();
                        if(cardArrayAdapter.getCount()==0){
                            Toast.makeText(EventsActivity.this,"No events to display",Toast.LENGTH_LONG).show();
                        }
                        break;

                    case 3:
                        EventsActivity.this.cardArrayAdapter = null;
                        cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_card);
                        EventsActivity.this.cardArrayAdapter.notifyDataSetChanged();

                        year = Calendar.getInstance().get(Calendar.YEAR);

                        for (i = 0; i<date.size(); i++) {
                            if (date.get(i).contains(String.valueOf(year))) {

                                int month = Calendar.getInstance().get(Calendar.MONTH)+1;
                                String exp = month+"/"+ year;

                                if(month<10)
                                    exp = "0"+month+"/"+year;


                                if(date.get(i).contains(exp))
                                {
                                    Log.d("Events", exp+"\t"+subject.get(i));

                                    Card card = new Card(time.get(i), venue.get(i), date.get(i), subject.get(i), ids.get(i));
                                    EventsActivity.this.cardArrayAdapter.add(card);
                                }
                            }
                        }
                        EventsActivity.this.cardArrayAdapter.notifyDataSetChanged();
                        if(cardArrayAdapter.getCount()==0){
                            Toast.makeText(EventsActivity.this,"No events to display",Toast.LENGTH_LONG).show();
                        }
                        break;

                    case 4:
                        EventsActivity.this.cardArrayAdapter = null;
                        cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_card);
                        EventsActivity.this.cardArrayAdapter.notifyDataSetChanged();

                        year = Calendar.getInstance().get(Calendar.YEAR);

                        for (i = 0; i<date.size(); i++) {
                            if (date.get(i).contains(String.valueOf(year))) {
                                Card card = new Card(time.get(i), venue.get(i), date.get(i), subject.get(i), ids.get(i));
                                cardArrayAdapter.add(card);
                            }
                        }
                        EventsActivity.this.cardArrayAdapter.notifyDataSetChanged();
                        if(cardArrayAdapter.getCount()==0){
                            Toast.makeText(EventsActivity.this,"No events to display",Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                EventsActivity.this.cardArrayAdapter.clear();

                int year = Calendar.getInstance().get(Calendar.YEAR);
                int i;

                for (i = 0; i<date.size(); i++) {
                    if (date.get(i).contains(String.valueOf(year))) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Calendar c = Calendar.getInstance();
                        String today = sdf.format(c.getTime());

                        Log.d("Events", today);

                        if (date.get(i).equals(today)) {
                            Card card = new Card(time.get(i), venue.get(i), date.get(i), subject.get(i), ids.get(i));
                            cardArrayAdapter.add(card);
                        }
                    }
                }
                EventsActivity.this.cardArrayAdapter.notifyDataSetChanged();
            }
        });

        String name = sharedPref.getString(PREF_ACCOUNT_NAME,"Account Name");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("events/"+name.hashCode());

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    HashMap hashMap = (HashMap) ds.getValue();

                    ArrayList<String> temp = new ArrayList<>();
                    temp = (ArrayList<String>) hashMap.get("date");

                    for (String s : temp){
                        date.add(s);
                        ids.add(ds.getKey());
                    }

                    temp = (ArrayList<String>) hashMap.get("subject");
                    for (String s : temp){subject.add(s);}
                    temp = (ArrayList<String>) hashMap.get("time");
                    for (String s : temp){time.add(s);}
                    temp = (ArrayList<String>) hashMap.get("venue");
                    for (String s : temp){venue.add(s);}

                    /*
                    int  i = date.indexOf("");
                    if(i!=-1) {
                        date.remove(i);
                        subject.remove(i);
                        time.remove(i);
                        venue.remove(i);
                    }
                     */
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        compactCalendarView = findViewById(R.id.compactcalendar_view);
        compactCalendarView.setUseThreeLetterAbbreviation(true);

        /* Event parameters :
         * 1. Color to be shown as dot in calender
         * 2. Epoch Date and Time format(Date and Time both should be passed to this function)
         * 3. Event title.*/
        //event set on 5th April 2020 at 11:51:30. L is for 'Long'
        Event event;

        for(String d : date)
        {
            if(!d.equals(""))
            {

                long t = 0;
                try {
                    t = dateToEpoch(d);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.d("Events", String.valueOf(t));

                int index = date.indexOf(d);
                event = new Event(Color.GREEN, t, subject.get(index));
                compactCalendarView.addEvent(event);
            }
        }

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                String d = format.format(dateClicked);

                if(date.contains(d))
                {
                    int index = date.indexOf(d);
                    Toast.makeText(EventsActivity.this, subject.get(index), Toast.LENGTH_LONG).show();
                }

                    /*
                dateClicked.toString() returns "Sun(Day) Apr(Month) 5(Date) 00:00:00(Time) GMT+05:30(Zone) 2020(Year)"
                We have to add events in database with above format and fetch them when date is clicked.
                When no time is in the event, we can set time as 00:00:00.
                dateClicked.toString() does not return an event. It just returns date clicked with 00:00:00 time.
                I think the event table should be as follows:
                1. Epoch DateTime(Time as 00:00:00)  2.Title of Event    3.Time of Event    4.Venue
                Whenever there we click a date , we convert it to Epoch format.
                Check for that epoch value in our database.
                No primary key for the database.
                If matches, get all other values and display maybe on another activity.
                */
            }

            //function to display month and year in the title bar.
            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                toolbar.setTitle(simpleDateFormat.format(firstDayOfNewMonth));
            }

        });
    }

    String epochToDate(long epochTime){
        Date date = new Date(epochTime);
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  //Can set the date-time format here
        format.setTimeZone(TimeZone.getTimeZone("IST"));
        String formatted = format.format(date);
        return formatted;
    }

    long dateToEpoch(String dateStr) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"); //should not be changed. Same format is returned by date onclick
        Date date = df.parse(dateStr);
        long epoch = date.getTime();
        return epoch;
    }
}
/*
        //calender event display
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mService = null;
        sharedPref = View_type.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);

        String accountName = sharedPref.getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
             mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("Mailbox App")
                    .build();

        } else {
            startActivity(new Intent(View_type.this, MainActivity.class));
            ActivityCompat.finishAffinity(View_type.this);
        }

        try {
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();
            if (items.isEmpty()) {
                //System.out.println("No upcoming events found.");
                TextView textView = findViewById(R.id.event);
                textView.setText(R.string.noevent);
            } else {
                LinearLayout linearLayout = findViewById(R.id.event_list);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                for (Event event : items) {
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) {
                        start = event.getStart().getDate();
                    }
                    TextView textView = new TextView(this);
                    textView.setLayoutParams(layoutParams);
                    textView.setText(event.getSummary());
                    linearLayout.addView(textView);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        */