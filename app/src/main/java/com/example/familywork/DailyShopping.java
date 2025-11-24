package com.example.familywork;

import java.util.List;

public class DailyShopping {
    private String dayKey;
    private List<Item> items;

    public DailyShopping() {}

    public DailyShopping(String dayKey, List<Item> items) {
        this.dayKey = dayKey;
        this.items = items;
    }

    public String getDayKey() { return dayKey; }
    public void setDayKey(String dayKey) { this.dayKey = dayKey; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}
