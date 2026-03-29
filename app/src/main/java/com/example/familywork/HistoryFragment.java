package com.example.familywork;

import android.content.Context;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private DatabaseReference historyRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        // שליפת קוד משפחה
        String familyCode = requireActivity()
                .getSharedPreferences("app", Context.MODE_PRIVATE)
                .getString("familyCode", "");

        if (!familyCode.isEmpty()) {
            historyRef = FirebaseDatabase.getInstance()
                    .getReference("families")
                    .child(familyCode)
                    .child("history");

            loadHistory();
        }
    }

    private void loadHistory() {

        historyRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // ממוין מהחדש לישן
                Map<String, List<Item>> historyData =
                        new TreeMap<>(Collections.reverseOrder());

                SimpleDateFormat sdf =
                        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                Date today = new Date();

                for (DataSnapshot dateSnap : snapshot.getChildren()) {

                    String dateKey = dateSnap.getKey();
                    if (dateKey == null) continue;

                    // בדיקת 7 ימים
                    try {
                        Date historyDate = sdf.parse(dateKey);
                        if (historyDate != null) {

                            long diffMillis =
                                    today.getTime() - historyDate.getTime();

                            long days =
                                    TimeUnit.DAYS.convert(diffMillis,
                                            TimeUnit.MILLISECONDS);

                            if (days > 7) {
                                // מוחק תאריך ישן
                                dateSnap.getRef().removeValue();
                                continue;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // איסוף פריטים של אותו יום
                    List<Item> itemsForDay = new ArrayList<>();

                    for (DataSnapshot itemSnap : dateSnap.getChildren()) {
                        Item item = itemSnap.getValue(Item.class);
                        if (item != null) {
                            itemsForDay.add(item);
                        }
                    }

                    if (!itemsForDay.isEmpty()) {
                        historyData.put(dateKey, itemsForDay);
                    }
                }

                adapter.updateData(historyData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}