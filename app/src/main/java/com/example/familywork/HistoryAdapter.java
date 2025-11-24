package com.example.familywork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private final List<DailyShopping> list;

    public HistoryAdapter(List<DailyShopping> list) { this.list = list; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DailyShopping ds = list.get(position);
        holder.dayTitle.setText(Utils.dayTitle(ds.getDayKey()));
        StringBuilder sb = new StringBuilder();
        for (Item it : ds.getItems()) {
            sb.append("• ").append(it.getName()).append(" — ").append(it.getQuantity()).append("\n");
        }
        holder.dayItems.setText(sb.toString());
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView dayTitle, dayItems;
        VH(@NonNull View itemView) {
            super(itemView);
            dayTitle = itemView.findViewById(R.id.textDayTitle);
            dayItems = itemView.findViewById(R.id.textDayItems);
        }
    }
}
