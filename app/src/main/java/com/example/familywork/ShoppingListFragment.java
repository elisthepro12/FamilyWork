package com.example.familywork;

import static android.content.Context.MODE_PRIVATE;

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

        // ---- Firebase לפי קוד משפחה ----
        String familyCode = getActivity()
                .getSharedPreferences("app", MODE_PRIVATE)
                .getString("familyCode", null);

        shoppingRef = FirebaseDatabase.getInstance()
                .getReference("families")
                .child(familyCode)
                .child("shoppingList");

        historyRef = FirebaseDatabase.getInstance()
                .getReference("families")
                .child(familyCode)
                .child("history"); // היסטוריה משפחתית אמיתית


        attachFirebaseListeners();

        // TextToSpeech
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("he"));
        });

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
        fabReadAll.setOnClickListener(v -> readAllItems());
    }

    private void attachFirebaseListeners() {
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

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
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

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
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
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_item, null);

        EditText etName = dialogView.findViewById(R.id.inputName);
        EditText etQty = dialogView.findViewById(R.id.inputQuantity);

        if (editItem != null) {
            etName.setText(editItem.getName());
            etQty.setText(String.valueOf(editItem.getQuantity()));
        }

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setTitle(editItem == null ? "הוספת מוצר" : "עריכת מוצר")
                .setPositiveButton(editItem == null ? "הוסף" : "שמור", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String qtyStr = etQty.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(qtyStr)) {
                        Toast.makeText(getContext(), "אנא מלא שם וכמות", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int qty = Integer.parseInt(qtyStr);

                    if (editItem == null) {
                        DatabaseReference push = shoppingRef.push();
                        Item newItem = new Item(push.getKey(), name, qty);
                        push.setValue(newItem);
                    } else {
                        editItem.setName(name);
                        editItem.setQuantity(qty);
                        shoppingRef.child(editItem.getId()).setValue(editItem);
                    }
                })
                .setNegativeButton("ביטול", (d, w) -> d.dismiss())
                .show();
    }

    private void onItemShortClick(Item item, int position) {
        String txt = item.getName() + " כמות " + item.getQuantity();
        tts.speak(txt, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void onItemLongClick(Item item, int position) {

        CharSequence[] options = {"ערוך", "מחק", "בטל"};

        new AlertDialog.Builder(getContext())
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        showAddEditDialog(item);
                    }

                    else if (which == 1) {
                        // ------ הוספה להיסטוריה משפחתית ------
                        historyRef.push().setValue(item);

                        // ------ מחיקה מהרשימה ------
                        shoppingRef.child(item.getId()).removeValue();
                    }

                    dialog.dismiss();
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
            sb.append(it.getName())
                    .append(" כמות ")
                    .append(it.getQuantity())
                    .append(". ");
        }

        tts.speak(sb.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
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
