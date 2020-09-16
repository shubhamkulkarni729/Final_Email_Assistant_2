package com.cognition.android.mailboxapp;

import com.cognition.android.mailboxapp.models.Message;

public class FirebaseMessage {
    String id;
    String summary;
    String category;
    String body;
    String from;
    String subject;

    public FirebaseMessage() {
    }

    public  FirebaseMessage(Message message) {
        this.id = message.getMessage_id();
        this.category = "Others";
        this.body = message.getBody();
        this.from = message.getFrom();
        this.subject = message.getSubject();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
