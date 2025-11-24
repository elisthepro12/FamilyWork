package com.example.familywork;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerHistory;
    private HistoryAdapter adapter;
    private List<DailyShopping> historyList = new ArrayList<>();
    private DatabaseReference historyRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerHistory = view.findViewById(R.id.recyclerHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(historyList);
        recyclerHistory.setAdapter(adapter);

        historyRef = FirebaseDatabase.getInstance().getReference("history");
        loadHistoryFromFirebase();
    }

    private void loadHistoryFromFirebase() {
        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot daySnap : snapshot.getChildren()) {
                    String dayKey = daySnap.getKey();
                    List<Item> dayItems = new ArrayList<>();
                    for (DataSnapshot itemSnap : daySnap.getChildren()) {
                        Item it = itemSnap.getValue(Item.class);
                        if (it != null) dayItems.add(it);
                    }
                    if (!dayItems.isEmpty()) historyList.add(new DailyShopping(dayKey, dayItems));
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
