package com.cognition.android.mailboxapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cognition.android.mailboxapp.activities.PreferenceListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSCardAdapter extends ArrayAdapter<SMS_Card> {
    private List<SMS_Card> cardList = new ArrayList<>();

    static class CardViewHolder {
        TextView line1;
        TextView label;
        TextView line2;
        TextView date;
        TextView balance;
        Button billbtn;
    }

    public SMSCardAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public void add(SMS_Card object) {
        cardList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.cardList.size();
    }

    @Override
    public SMS_Card getItem(int index) {
        return this.cardList.get(index);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        CardViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.card_sms, parent, false);
            viewHolder = new CardViewHolder();
            viewHolder.line1 =  row.findViewById(R.id.line1);
            viewHolder.label =  row.findViewById(R.id.label);
            viewHolder.line2 =  row.findViewById(R.id.line2);
            viewHolder.date = row.findViewById(R.id.sms_date);
            viewHolder.balance = row.findViewById(R.id.avl_balance);
            viewHolder.billbtn=  row.findViewById(R.id.billbtn);
            row.setTag(viewHolder);
        } else {
            viewHolder = (CardViewHolder)row.getTag();
        }
        SMS_Card card = getItem(position);
        viewHolder.line1.setText("From : "+card.getLine1());      //sender

        if(card.getLabel().equals("")) viewHolder.label.setVisibility(View.INVISIBLE); //label
        else {

            viewHolder.label.setVisibility(View.VISIBLE);
            viewHolder.label.setText(card.getLabel());
        }

        viewHolder.line2.setText(card.getLine2());  //message body
        viewHolder.date.setText(card.getDate());    //message date

         if(card.getBalance().equals(""))   viewHolder.balance.setVisibility(View.INVISIBLE);   //balance
         else {
             viewHolder.balance.setVisibility(View.VISIBLE);
             viewHolder.balance.setText("Balance : "+card.getBalance());
         }

        String paybtnvisibility = card.getLabel();
        Pattern bill = Pattern.compile("\\bBILL\\b");
        Matcher matcher = bill.matcher(paybtnvisibility);
        if(matcher.find()){
            viewHolder.billbtn.setVisibility(View.VISIBLE);
            Log.i("check if loop","label present");

            viewHolder.billbtn.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), PaymentAppChoserActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);;
                String title = "Choose Payment App";
                Intent chooser = Intent.createChooser(intent, title).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(chooser);
            });

        }
        else{
            viewHolder.billbtn.setVisibility(View.INVISIBLE);
        }

        return row;
    }
}
