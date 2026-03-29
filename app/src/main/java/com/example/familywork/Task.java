package com.example.familywork;

import java.util.HashMap;
import java.util.Map;

public class Task {

    private String id;
    private String title;
    private boolean done;
    private boolean daily;

    private Map<String,String> owners;

    public Task(){}

    public Task(String title){
        this.title = title;
        this.done = false;
        this.daily = false;
        this.owners = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isDaily() {
        return daily;
    }

    public void setDaily(boolean daily) {
        this.daily = daily;
    }

    public Map<String, String> getOwners() {
        return owners;
    }

    public void setOwners(Map<String, String> owners) {
        this.owners = owners;
    }
}