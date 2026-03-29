// קובץ חדש: HistoryItem.java
package com.example.familywork;

public class HistoryItem extends Item implements HistoryListItem {
    @Override
    public int getItemType() {
        return TYPE_ITEM;
    }
}
