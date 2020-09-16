package com.cognition.android.mailboxapp;

public class Transaction {
    String Amount, Date;

    public Transaction(String amount, String date) {
        Amount = amount;
        Date = date;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
