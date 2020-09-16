package com.cognition.android.mailboxapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.cognition.android.mailboxapp.activities.EmailActivity;
import com.cognition.android.mailboxapp.models.Message;
import com.cognition.android.mailboxapp.models.Message_Table;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;
import com.raizlabs.android.dbflow.sql.language.SQLite;

@Layout(R.layout.tinder_card_view)
public class TinderCard {

    @View(R.id.subject)
    TextView subject;

    @View(R.id.sender)
    TextView sender;

    @View(R.id.message)
    TextView message;

    @View(R.id.email_category)
    TextView category;

    Profile mProfile;
    Context mContext;
    SwipePlaceHolderView mSwipeView;

    public TinderCard(Context context, Profile profile, SwipePlaceHolderView swipeView) {
        mContext = context;
        mProfile = profile;
        mSwipeView = swipeView;
    }

    /*@Click(R.id.read_but)
    public void Read(){
        mSwipeView.doSwipe(true);
        Message message = SQLite.select().from(Message.class).where(Message_Table.id.eq(Integer.valueOf(mProfile.getId()))).querySingle();
        message.setRead(true);
        message.update();
    }

    @Click(R.id.delete_but)
    public void Delete(){
        mSwipeView.doSwipe(false);
        Message message = SQLite.select().from(Message.class).where(Message_Table.id.eq(Integer.valueOf(mProfile.getId()))).querySingle();
        message.setRead(true);
        message.update();
    }
     */
    @Resolve
    public void onResolved(){
        subject.setText(mProfile.getSubject());
        sender.setText(mProfile.getSender());
        message.setText(mProfile.getMessage());
        category.setText(mProfile.getCategory());
    }

    @SwipeOut
    public void onSwipedOut(){
        Log.d("EVENT", "onSwipedOut");
        //mSwipeView.addView(this);
        Message message = SQLite.select().from(Message.class).where(Message_Table.id.eq(Integer.valueOf(mProfile.getId()))).querySingle();
        message.setRead(true);
        message.update();
    }

    @SwipeCancelState
    public void onSwipeCancelState(){
        Log.d("EVENT", "onSwipeCancelState");
    }

    @SwipeIn
    public void onSwipeIn(){
        Log.d("EVENT", "onSwipedIn");
        Message message = SQLite.select().from(Message.class).where(Message_Table.id.eq(Integer.valueOf(mProfile.getId()))).querySingle();
        message.setRead(true);
        message.update();
    }

    @SwipeInState
    public void onSwipeInState(){
        Log.d("EVENT", "onSwipeInState");
    }

    @SwipeOutState
    public void onSwipeOutState(){
        Log.d("EVENT", "onSwipeOutState");
    }

}