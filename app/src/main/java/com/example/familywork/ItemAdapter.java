package com.example.familywork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private ArrayList<String> itemList;

    public ItemAdapter(ArrayList<String> itemList){
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position){
        String item = itemList.get(position);
        holder.textView.setText(item);
        int color = position % 2 == 0 ? 0xFFE0F7FA : 0xFFFFF9C4; // צבעים לסירוגין
        holder.textView.setBackgroundColor(color);
    }

    @Override
    public int getItemCount(){
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public ItemViewHolder(@NonNull View itemView){
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
