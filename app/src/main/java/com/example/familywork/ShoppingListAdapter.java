package com.example.familywork;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShoppingListAdapter
        extends RecyclerView.Adapter<ShoppingListAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(Item item, int position);
    }

    public interface OnEditClickListener {
        void onEditClick(Item item, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Item item, int position);
    }

    private final Context context;
    private final List<Item> list;
    private final OnItemClickListener clickListener;
    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;

    public ShoppingListAdapter(
            Context context,
            List<Item> list,
            OnItemClickListener clickListener,
            OnEditClickListener editListener,
            OnDeleteClickListener deleteListener
    ) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_shopping, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Item item = list.get(position);

        holder.name.setText(item.getName());
        holder.quantity.setText(String.valueOf(item.getQuantity()));

        holder.container.setOnClickListener(v ->
                clickListener.onItemClick(item, position));

        holder.container.setOnLongClickListener(v -> {
            deleteListener.onDeleteClick(item, position);
            return true;
        });

        holder.btnEdit.setOnClickListener(v ->
                editListener.onEditClick(item, position));

        holder.btnDelete.setOnClickListener(v ->
                deleteListener.onDeleteClick(item, position));

        if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
            holder.btnImage.setVisibility(View.VISIBLE);
            holder.btnImage.setOnClickListener(v ->
                    showImageDialog(item.getImageBase64()));
        } else {
            holder.btnImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView name, quantity;
        ImageButton btnEdit, btnDelete, btnImage;

        VH(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.itemLayout);
            name = itemView.findViewById(R.id.itemName);
            quantity = itemView.findViewById(R.id.itemQuantity);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnImage = itemView.findViewById(R.id.btnImage);
        }
    }

    private void showImageDialog(String base64) {
        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);

        new AlertDialog.Builder(context)
                .setView(imageView)
                .setPositiveButton("סגור", null)
                .show();
    }
}
