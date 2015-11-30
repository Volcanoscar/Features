package com.mlt.floatmultitask;

/**
 * Created by laiyang on 15-7-9.
 */
public class SmsInfo {

    private String smsbody;

    private String phoneNumber;

    private String date;

    private String name;

    private String type;

    private int id;

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    private int threadId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSmsbody(String smsbody) {
        this.smsbody = smsbody;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSmsbody() {
        return smsbody;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

}
