package com.example.familywork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(Item item, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Item item, int position);
    }

    private final Context context;
    private final List<Item> list;
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public ShoppingListAdapter(Context ctx, List<Item> list,
                               OnItemClickListener clickListener,
                               OnItemLongClickListener longClickListener) {
        this.context = ctx;
        this.list = list;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_shopping, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Item item = list.get(position);
        holder.name.setText(item.getName());
        holder.quantity.setText(String.valueOf(item.getQuantity()));

        // צבע יציב לפי השם
        holder.container.setBackgroundColor(getColorForItem(item.getName()));

        holder.container.setOnClickListener(v -> clickListener.onItemClick(item, position));
        holder.container.setOnLongClickListener(v -> {
            longClickListener.onItemLongClick(item, position);
            return true;
        });

        holder.btnEdit.setOnClickListener(v -> longClickListener.onItemLongClick(item, position)); // reuse long click to open edit/delete dialog
        holder.btnDelete.setOnClickListener(v -> longClickListener.onItemLongClick(item, position));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView name, quantity;
        ImageButton btnEdit, btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.itemLayout);
            name = itemView.findViewById(R.id.itemName);
            quantity = itemView.findViewById(R.id.itemQuantity);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private int getColorForItem(String itemName) {
        int[] colors = {
                0xFFFFCDD2, 0xFFF8BBD0, 0xFFE1BEE7, 0xFFD1C4E9, 0xFFC5CAE9,
                0xFFBBDEFB, 0xFFB2EBF2, 0xFFC8E6C9, 0xFFFFF9C4, 0xFFFFE0B2
        };
        return colors[Math.abs(itemName.hashCode()) % colors.length];
    }
}
