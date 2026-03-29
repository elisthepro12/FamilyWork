package com.example.familywork;

import java.util.List;

public class HstoryDay {

    public String dayKey;
    public List<Item> items;

    public HstoryDay(String dayKey, List<Item> items) {
        this.dayKey = dayKey;
        this.items = items;
    }
}
