package com.cognition.android.mailboxapp;
public class Card {
    private String id;
    private String time;
    private String venue;
    private String date;
    private String subject;

    public Card(String time, String venue, String date, String subject, String id) {
        this.id = id;
        this.time = time;
        this.venue = venue;
        this.date = date;
        this.subject = subject;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
