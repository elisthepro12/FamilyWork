// קובץ חדש: DateItem.java
package com.example.familywork;

public class DateItem implements HistoryListItem {
    private String date;

    public DateItem(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int getItemType() {
        return TYPE_DATE;
    }
}
