package com.cognition.android.mailboxapp;

public class EmailPreferenceObject {

    private String emailAddress;
    private boolean preferred;

    EmailPreferenceObject(String email, boolean preferred){
        this.emailAddress = email;
        this.preferred = preferred;
    }

    public String getEmailAddress(){
        return this.emailAddress;
    }

    public boolean getPreferred(){
        return this.preferred;
    }
}
