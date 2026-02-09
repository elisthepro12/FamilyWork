package com.example.familywork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // רשימה שמכילה גם מחרוזות (לתאריך) וגם אובייקטי Item
    private final List<Object> displayList = new ArrayList<>();

    public void updateData(Map<String, List<Item>> historyMap) {
        displayList.clear();
        if (historyMap != null) {
            for (Map.Entry<String, List<Item>> entry : historyMap.entrySet()) {
                // הוספת כותרת היום
                displayList.add(formatDateToHebrew(entry.getKey()));
                // הוספת המוצרים של אותו יום
                displayList.addAll(entry.getValue());
            }
        }
        notifyDataSetChanged();
    }

    private String formatDateToHebrew(String dateString) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = in.parse(dateString);
            SimpleDateFormat out = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("he", "IL"));
            return out.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (displayList.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_history_day, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_history, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object data = displayList.get(position);

        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).txtDate.setText((String) data);
        } else if (holder instanceof ItemVH) {
            Item item = (Item) data;
            ItemVH vh = (ItemVH) holder;
            vh.txtName.setText(item.getName());
            vh.txtQty.setText("כמות: " + item.getQuantity());

            // הגדרת צבע רקע לפי השם (אופציונלי)
            vh.itemView.setBackgroundColor(getColorForItem(item.getName()));
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    private int getColorForItem(String name) {
        int[] colors = {0xFFFFCDD2, 0xFFF8BBD0, 0xFFE1BEE7, 0xFFD1C4E9, 0xFFC5CAE9, 0xFFBBDEFB, 0xFFC8E6C9};
        return colors[Math.abs(name.hashCode()) % colors.length];
    }

    // --- ViewHolders ---

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView txtDate;
        HeaderVH(View v) {
            super(v);
            txtDate = v.findViewById(R.id.historyDayTitle);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView txtName, txtQty;
        ItemVH(View v) {
            super(v);
            txtName = v.findViewById(R.id.historyItemName);
            txtQty = v.findViewById(R.id.historyItemQty);
        }
    }
}