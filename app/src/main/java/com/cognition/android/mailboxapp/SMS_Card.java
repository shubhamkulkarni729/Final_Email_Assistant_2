package com.cognition.android.mailboxapp;

import android.widget.Button;

public class SMS_Card {
    private String line1;
    private String label;
    private String line2;
    private String date;
    private String balance;
    private Button billbtn;

    public SMS_Card(String line1, String label, String line2, Button billbtn, String date, String balance) {
        this.line1 = line1;
        this.label = label;
        this.line2 = line2;
        this.billbtn = billbtn;
        this.date = date;
        this.balance = balance;
    }

    public String getLine1() {
        return line1;
    }
    public String getLabel() {
        return label;
    }
    public String getLine2() {
        return line2;
    }
    public String getDate() {return date;}
    public String getBalance() {return balance;}
}
