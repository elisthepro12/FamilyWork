package com.example.familywork;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import com.example.familywork.Item;
public class ShoppingListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private List<Item> itemList;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        Button btnAdd = view.findViewById(R.id.btn_add_item);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemList = new ArrayList<Item>();
        adapter = new ShoppingListAdapter(getContext(), itemList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("shopping_list");

        loadItems();

        btnAdd.setOnClickListener(v -> showAddItemDialog());

        return view;
    }

    private void loadItems() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    Item item = itemSnap.getValue(Item.class);
                    if (item != null) itemList.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("הוסף מוצר חדש");

        View dialogView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, null);
        EditText inputName = new EditText(getContext());
        inputName.setHint("שם מוצר");
        EditText inputQty = new EditText(getContext());
        inputQty.setHint("כמות");
        inputQty.setInputType(InputType.TYPE_CLASS_NUMBER);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);
        layout.addView(inputName);
        layout.addView(inputQty);
        builder.setView(layout);

        builder.setPositiveButton("שמור", (dialog, which) -> {
            String name = inputName.getText().toString();
            String qtyStr = inputQty.getText().toString();
            if (!name.isEmpty() && !qtyStr.isEmpty()) {
                String id = databaseReference.push().getKey();
                Item newItem = new Item(id, name, Integer.parseInt(qtyStr));
                databaseReference.child(id).setValue(newItem);
            }
        });

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }
}
