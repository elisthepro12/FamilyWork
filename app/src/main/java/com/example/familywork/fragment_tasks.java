package com.example.familywork;

import android.content.Context;
import android.os.Bundle;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// פרגמנט המשימות - כאן אנחנו מנהלים את רשימת המטלות של המשפחה
public class fragment_tasks extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private Button btnDeleteDone;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private DatabaseReference tasksRef;
    private String familyCode;

    // רשימת קטגוריות מוגדרת מראש כדי לעזור למשתמש למיין משימות
    private final String[] categories = {"כללי", "כלב", "אוכל", "מטבח", "מטלות בית", "חדר אישי"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ניפוח ה-XML של הפרגמנט
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // קישור רכיבי הממשק
        recyclerView = view.findViewById(R.id.recyclerTasks);
        fabAdd = view.findViewById(R.id.fabAddTask);
        btnDeleteDone = view.findViewById(R.id.btnDeleteDone);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // שליפת קוד המשפחה הנוכחי מהזיכרון המקומי
        familyCode = requireActivity().getSharedPreferences("app", Context.MODE_PRIVATE).getString("familyCode", "");
        
        // אתחול המתאם (Adapter) והגדרת לחיצה על משימה לעריכה
        adapter = new TaskAdapter(taskList, familyCode);
        adapter.setOnTaskClickListener(task -> showTaskDialog(task));
        recyclerView.setAdapter(adapter);

        // חיבור ל-Firebase תחת המיקום הספציפי של משימות המשפחה הזו
        tasksRef = FirebaseDatabase.getInstance().getReference("families").child(familyCode).child("tasks");
        
        // טעינת המשימות מהענן
        loadTasks();

        // כפתור להוספת משימה חדשה
        fabAdd.setOnClickListener(v -> showTaskDialog(null)); 
        
        // כפתור למחיקת כל המשימות שכבר סומנו כ"בוצעו" (ניקוי הרשימה)
        btnDeleteDone.setOnClickListener(v -> {
            for (Task t : taskList) {
                if (t.isDone()) tasksRef.child(t.getId()).removeValue();
            }
        });
    }

    // פונקציה שמאזינה לשינויים ב-Firebase ומעדכנת את הרשימה בכל פעם שמשהו משתנה
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
                adapter.updateTasks(taskList); // עדכון התצוגה למשתמש
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // פתיחת דיאלוג (חלון קופץ) להוספה או עריכה של משימה
    private void showTaskDialog(@Nullable Task taskToEdit) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task, null);
        TextInputEditText inputTitle = dialogView.findViewById(R.id.inputTaskTitle);
        CheckBox dailyCheck = dialogView.findViewById(R.id.checkDaily);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        
        // קונטיינר אליו נוסיף תיבות סימון עבור כל בן משפחה
        LinearLayout containerMembers = dialogView.findViewById(R.id.taskMembersLayout);

        // הגדרת הספינר של הקטגוריות
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        // אם אנחנו במצב עריכה, נמלא את השדות בנתונים הקיימים
        if (taskToEdit != null) {
            inputTitle.setText(taskToEdit.getTitle());
            dailyCheck.setChecked(taskToEdit.isDaily());
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(taskToEdit.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }

        // שליפת רשימת בני המשפחה מה-Firebase כדי שנוכל לשייך אותם למשימה
        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("families").child(familyCode).child("info");
        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, CheckBox> checkBoxes = new HashMap<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String name = snap.child("name").getValue(String.class);
                    String id = snap.getKey();
                    if (name != null && id != null) {
                        CheckBox cb = new CheckBox(getContext());
                        cb.setText(name);
                        cb.setTextSize(16);
                        // אם בעריכה המשתמש כבר משויך למשימה, נסמן את התיבה שלו
                        if (taskToEdit != null && taskToEdit.getOwners() != null && taskToEdit.getOwners().containsKey(id)) {
                            cb.setChecked(true);
                        }
                        containerMembers.addView(cb);
                        checkBoxes.put(id, cb);
                    }
                }

                // יצירת והצגת הדיאלוג
                new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .setTitle(taskToEdit == null ? "מטלה חדשה" : "עריכת מטלה")
                        .setPositiveButton("שמור", (d, w) -> {
                            String title = inputTitle.getText().toString().trim();
                            if (title.isEmpty()) return;

                            // בניית מפת המשתמשים שנבחרו למשימה (Owners)
                            Map<String, String> owners = new HashMap<>();
                            for (Map.Entry<String, CheckBox> entry : checkBoxes.entrySet()) {
                                if (entry.getValue().isChecked()) {
                                    owners.put(entry.getKey(), entry.getValue().getText().toString());
                                }
                            }

                            // יצירת אובייקט משימה חדש ושמירתו ב-Firebase
                            String id = (taskToEdit == null) ? tasksRef.push().getKey() : taskToEdit.getId();
                            Task task = new Task(title);
                            task.setId(id);
                            task.setOwners(owners);
                            task.setDaily(dailyCheck.isChecked());
                            task.setCategory(spinnerCategory.getSelectedItem().toString());

                            if (id != null) tasksRef.child(id).setValue(task);
                        })
                        .setNegativeButton("ביטול", null)
                        .show();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
