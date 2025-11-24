package com.example.familywork;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static HistoryManager instance;
    private List<Item> historyList;

    private HistoryManager() {
        historyList = new ArrayList<>();
    }

    public static HistoryManager getInstance() {
        if (instance == null)
            instance = new HistoryManager();
        return instance;
    }

    public void addToHistory(Item item) {
        historyList.add(item);
    }

    public List<Item> getHistoryList() {
        return historyList;
    }
}
