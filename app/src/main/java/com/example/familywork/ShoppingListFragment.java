package com.example.familywork;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShoppingListFragment extends Fragment {

    private RecyclerView recycler;
    private ShoppingListAdapter adapter;
    private List<Item> items;

    private FloatingActionButton fabAdd, fabReadAll;
    private TextToSpeech tts;

    // Firebase refs
    private DatabaseReference shoppingRef;
    private DatabaseReference historyRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recycler = view.findViewById(R.id.recyclerShopping);
        fabAdd = view.findViewById(R.id.fabAdd);
        fabReadAll = view.findViewById(R.id.fabReadAll);

        items = new ArrayList<>();
        adapter = new ShoppingListAdapter(getContext(), items,
                this::onItemShortClick, this::onItemLongClick);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        // Firebase
        shoppingRef = FirebaseDatabase.getInstance().getReference("shoppingList");
        historyRef = FirebaseDatabase.getInstance().getReference("history");

        // קבלת נתונים בזמן-אמת
        attachFirebaseListeners();

        // TextToSpeech
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("he"));
        });

        fabAdd.setOnClickListener(v -> showAddEditDialog(null)); // null => הוספה
        fabReadAll.setOnClickListener(v -> readAllItems());
    }

    private void attachFirebaseListeners() {
        // child event listener כדי לקבל מעקב לפי מפתחות
        shoppingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Item item = snapshot.getValue(Item.class);
                if (item != null) {
                    item.setId(snapshot.getKey());
                    items.add(item);
                    adapter.notifyItemInserted(items.size() - 1);
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Item newItem = snapshot.getValue(Item.class);
                if (newItem == null) return;
                String key = snapshot.getKey();
                for (int i = 0; i < items.size(); i++) {
                    Item it = items.get(i);
                    if (it.getId() != null && it.getId().equals(key)) {
                        newItem.setId(key);
                        items.set(i, newItem);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();
                for (int i = 0; i < items.size(); i++) {
                    Item it = items.get(i);
                    if (it.getId() != null && it.getId().equals(key)) {
                        items.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddEditDialog(@Nullable Item editItem) {
        // אם editItem == null => הוספה חדשה, אחרת עריכה
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        EditText etName = dialogView.findViewById(R.id.inputName);
        EditText etQty = dialogView.findViewById(R.id.inputQuantity);

        if (editItem != null) {
            etName.setText(editItem.getName());
            etQty.setText(String.valueOf(editItem.getQuantity()));
        }

        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setView(dialogView);
        b.setTitle(editItem == null ? "הוספת מוצר" : "עריכת מוצר");
        b.setPositiveButton(editItem == null ? "הוסף" : "שמור", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQty.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(qtyStr)) {
                Toast.makeText(getContext(), "אנא מלא שם וכמות", Toast.LENGTH_SHORT).show();
                return;
            }
            int qty = 1;
            try { qty = Integer.parseInt(qtyStr); } catch (Exception ignored) {}

            if (editItem == null) {
                // הוספה ל-Firebase (key ייווצר אוטומטית)
                DatabaseReference pushed = shoppingRef.push();
                Item newItem = new Item(pushed.getKey(), name, qty);
                pushed.setValue(newItem);
            } else {
                // עריכה: עדכון בשדה לפי ה-id
                editItem.setName(name);
                editItem.setQuantity(qty);
                if (editItem.getId() != null) {
                    shoppingRef.child(editItem.getId()).setValue(editItem);
                }
            }
        });
        b.setNegativeButton("ביטול", (d, w) -> d.dismiss());
        b.show();
    }

    // לחיצה קצרה — קורא את המוצר
    private void onItemShortClick(Item item, int position) {
        String text = item.getName() + " כמות " + item.getQuantity();
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    // לחיצה ארוכה — פותח דיאלוג עם אפשרות עריכה / מחיקה
    private void onItemLongClick(Item item, int position) {
        CharSequence[] options = new CharSequence[] {"ערוך", "מחק", "בטל"};
        new AlertDialog.Builder(getContext())
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // עריכה
                        showAddEditDialog(item);
                    } else if (which == 1) {
                        // מחיקה: העברה להיסטוריה ו-הסרה מ-shoppingRef
                        String day = Utils.dayKey();
                        // העברה להיסטוריה (push)
                        historyRef.child(day).push().setValue(item, (err, ref) -> {
                            // לאחר שכתבנו להיסטוריה, נמחק מהרשימה (אם רוצים לוודא עקביות)
                            if (item.getId() != null) {
                                shoppingRef.child(item.getId()).removeValue();
                            }
                        });
                    } else {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void readAllItems() {
        if (items.isEmpty()) {
            Toast.makeText(getContext(), "הרשימה ריקה", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder("הרשימה: ");
        for (Item it : items) {
            sb.append(it.getName()).append(" כמות ").append(it.getQuantity()).append(". ");
        }
        if (tts != null) tts.speak(sb.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onDestroyView() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroyView();
    }
}
