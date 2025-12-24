package com.example.familywork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {

    private Context context;
    private List<Item> items;

    public HistoryAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_history, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Item item = items.get(position);
        holder.name.setText(item.getName());
        holder.qty.setText("כמות: " + item.getQuantity());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView name, qty;

        public Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.historyName);
            qty = itemView.findViewById(R.id.historyQty);
        }
    }
}
