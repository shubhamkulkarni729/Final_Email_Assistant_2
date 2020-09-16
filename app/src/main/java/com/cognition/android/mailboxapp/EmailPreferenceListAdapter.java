package com.cognition.android.mailboxapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cognition.android.mailboxapp.activities.MainActivity.PREFERRED_EMAIL_ADDRESSES;

public class EmailPreferenceListAdapter extends ArrayAdapter<EmailPreferenceObject> {

    private List<EmailPreferenceObject> emailPreferenceList = new ArrayList<>();
    private ArrayList<String>selectedEmails;
    SharedPreferences sharedPreferences;

    public EmailPreferenceListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        sharedPreferences = getContext().getSharedPreferences("com.cognition.android.mailboxapp.PREFERENCES_FILE", Context.MODE_PRIVATE);
        String previousPreferredEmailsString = sharedPreferences.getString(PREFERRED_EMAIL_ADDRESSES,"");
        String[] previousPreferredEmails = previousPreferredEmailsString.split(",");
        selectedEmails = new ArrayList<>(Arrays.asList(previousPreferredEmails));
        System.out.println("Constructor "+selectedEmails);
    }

    static class EmailPreferenceViewHolder{
        TextView textViewEmailAddress;
        CheckBox checkBoxPreferred;
    }

    @Override
    public void add(EmailPreferenceObject object){
        emailPreferenceList.add(object);
        super.add(object);
    }

    @Override
    public int getCount(){
        return this.emailPreferenceList.size();
    }

    @Override
    public EmailPreferenceObject getItem(int index){
        return this.emailPreferenceList.get(index);
    }

    public void setItem(int index, EmailPreferenceObject newObject){
        this.emailPreferenceList.set(index,newObject);
    }

    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        EmailPreferenceListAdapter.EmailPreferenceViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.email_list, parent, false);
            viewHolder = new EmailPreferenceListAdapter.EmailPreferenceViewHolder();
            viewHolder.textViewEmailAddress = row.findViewById(R.id.email_address_for_preference);
            viewHolder.checkBoxPreferred = row.findViewById(R.id.tick_for_email_preference);
            row.setTag(viewHolder);
        }
        else {
            viewHolder = (EmailPreferenceListAdapter.EmailPreferenceViewHolder)row.getTag();
        }

        EmailPreferenceObject emailPreferenceObject = getItem(position);
        boolean isChecked = emailPreferenceObject.getPreferred();

        viewHolder.textViewEmailAddress.setText(emailPreferenceObject.getEmailAddress());
        if(isChecked) viewHolder.checkBoxPreferred.setChecked(true);
        else viewHolder.checkBoxPreferred.setChecked(false);

        viewHolder.checkBoxPreferred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.checkBoxPreferred.isChecked()){
                    EmailPreferenceObject object = new EmailPreferenceObject(emailPreferenceObject.getEmailAddress(),true);
                    //setItem(position,object);
                    selectedEmails.add(emailPreferenceObject.getEmailAddress());
                }
                else {
                    EmailPreferenceObject object = new EmailPreferenceObject(emailPreferenceObject.getEmailAddress(),false);
                    //setItem(position,object);
                    selectedEmails.remove(emailPreferenceObject.getEmailAddress());
                }

                String selectedEmailsString = selectedEmails.toString();
                selectedEmailsString = selectedEmailsString.replace("[","");
                selectedEmailsString = selectedEmailsString.replace("]","");
                System.out.println(selectedEmailsString);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREFERRED_EMAIL_ADDRESSES,selectedEmailsString);
                editor.apply();
            }
        });

        return row;
    }
}