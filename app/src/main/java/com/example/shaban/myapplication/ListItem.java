package com.example.shaban.myapplication;

/**
 * Created by Osama on 3/29/2016.
 */
public class ListItem {
    private String id;
    private String subject;
    private String date;
    private String details;
    private boolean completed;


    ListItem(String id, String subject, String details, String time, boolean completed) {
        this.setId(id);
        this.date = time;
        this.subject = subject;
        this.completed = completed;
        this.details = details;

    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMasage() {
        return details;
    }

    public void setMasage(String details) {
        this.details = details;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
