package com.example.familywork;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class fragment_tasks extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private Button btnDeleteDone;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private DatabaseReference tasksRef;
    private String familyCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerTasks);
        fabAdd = view.findViewById(R.id.fabAddTask);
        btnDeleteDone = view.findViewById(R.id.btnDeleteDone);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        familyCode = requireActivity().getSharedPreferences("app", Context.MODE_PRIVATE).getString("familyCode", "");
        adapter = new TaskAdapter(taskList, familyCode);
        recyclerView.setAdapter(adapter);

        tasksRef = FirebaseDatabase.getInstance().getReference("families").child(familyCode).child("tasks");
        loadTasks();

        fabAdd.setOnClickListener(v -> showAddTaskDialog());
        btnDeleteDone.setOnClickListener(v -> {
            for (Task t : taskList) if (t.isDone()) tasksRef.child(t.getId()).removeValue();
        });
    }

    private void loadTasks() {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Task task = snap.getValue(Task.class);
                    if (task != null) {
                        task.setId(snap.getKey());
                        taskList.add(task);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddTaskDialog() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        EditText input = new EditText(getContext());
        input.setHint("שם המטלה");
        CheckBox dailyCheck = new CheckBox(getContext());
        dailyCheck.setText("מטלה יומית");
        layout.addView(input);
        layout.addView(dailyCheck);

        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("families").child(familyCode).child("info");
        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> names = new ArrayList<>();
                List<String> ids = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    names.add(snap.child("name").getValue(String.class));
                    ids.add(snap.getKey());
                }
                boolean[] checked = new boolean[names.size()];
                new AlertDialog.Builder(requireContext()).setTitle("מטלה חדשה").setView(layout)
                        .setMultiChoiceItems(names.toArray(new String[0]), checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                        .setPositiveButton("שמור", (d, w) -> {
                            String title = input.getText().toString().trim();
                            if (title.isEmpty()) return;
                            String id = tasksRef.push().getKey();
                            Map<String, String> owners = new HashMap<>();
                            for (int i = 0; i < checked.length; i++) if (checked[i]) owners.put(ids.get(i), names.get(i));

                            Task task = new Task(title);
                            task.setId(id);
                            task.setOwners(owners);
                            task.setDaily(dailyCheck.isChecked());
                            tasksRef.child(id).setValue(task);
                        })
                        .setNegativeButton("ביטול", null).show();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}