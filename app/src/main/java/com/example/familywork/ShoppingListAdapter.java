package com.example.familywork;

import android.app.AlertDialog;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private Context context;
    private List<Item> itemList;
    private TextToSpeech textToSpeech;

    public ShoppingListAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;

        // אתחול טקסט להקראה
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.forLanguageTag("he-IL"));
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.nameText.setText("שם המוצר: " + item.getName());
        holder.quantityText.setText("כמות: " + item.getQuantity());

        // צבע רקע קבוע לפי שם
        holder.itemLayout.setBackgroundColor(getColorForItem(item.getName()));

        // לחיצה קצרה - מקריא שם וכמות
        holder.itemView.setOnClickListener(v -> {
            String text = "שם המוצר: " + item.getName() + ", כמות: " + item.getQuantity();
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        });

        // לחיצה ארוכה - פותחת דיאלוג לעריכה/מחיקה
        holder.itemView.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("ערוך או מחק מוצר");

            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_item, null);
            EditText nameEdit = dialogView.findViewById(R.id.editName);
            EditText quantityEdit = dialogView.findViewById(R.id.editQuantity);
            nameEdit.setText(item.getName());
            quantityEdit.setText(String.valueOf(item.getQuantity()));

            builder.setView(dialogView);
            builder.setPositiveButton("שמור", (dialog, which) -> {
                item.setName(nameEdit.getText().toString());
                item.setQuantity(Integer.parseInt(quantityEdit.getText().toString()));
                notifyItemChanged(position);
                Toast.makeText(context, "הפריט עודכן", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("מחק", (dialog, which) -> {
                itemList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "הפריט נמחק", Toast.LENGTH_SHORT).show();
            });
            builder.setNeutralButton("ביטול", null);
            builder.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // מחזיר צבע ייחודי לכל פריט לפי שמו
    private int getColorForItem(String itemName) {
        int[] colors = {
                0xFFFFCDD2, 0xFFF8BBD0, 0xFFE1BEE7, 0xFFD1C4E9,
                0xFFC5CAE9, 0xFFBBDEFB, 0xFFB2EBF2, 0xFFC8E6C9,
                0xFFFFF9C4, 0xFFFFE0B2
        };
        int index = Math.abs(itemName.hashCode()) % colors.length;
        return colors[index];
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        TextView nameText, quantityText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            nameText = itemView.findViewById(R.id.itemName);
            quantityText = itemView.findViewById(R.id.itemQuantity);
        }
    }
}
