package com.example.familywork;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // 1. הגדרת הממשק ללחיצה
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    private List<Object> displayList = new ArrayList<>();
    private String familyCode;
    
    // 2. משתנה לשמירת המאזין
    private OnTaskClickListener listener;

    public TaskAdapter(List<Task> tasks, String familyCode) {
        this.familyCode = familyCode;
        updateTasks(tasks);
    }

    // 3. המתודה שקובעת את המאזין
    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void updateTasks(List<Task> tasks) {
        displayList.clear();
        if (tasks == null || tasks.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        Map<String, List<Task>> groupedTasks = new TreeMap<>();
        for (Task task : tasks) {
            String cat = task.getCategory();
            if (cat == null || cat.isEmpty()) cat = "כללי";
            if (!groupedTasks.containsKey(cat)) {
                groupedTasks.put(cat, new ArrayList<>());
            }
            groupedTasks.get(cat).add(task);
        }

        for (Map.Entry<String, List<Task>> entry : groupedTasks.entrySet()) {
            displayList.add(entry.getKey());
            displayList.addAll(entry.getValue());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (displayList.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayList.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).textHeader.setText((String) item);
        } else if (holder instanceof TaskViewHolder) {
            Task task = (Task) item;
            TaskViewHolder tHolder = (TaskViewHolder) holder;

            tHolder.title.setText(task.getTitle());

            if (task.getOwners() != null && !task.getOwners().isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (String name : task.getOwners().values()) {
                    names.append(name).append(" ");
                }
                tHolder.owners.setText("אחראי: " + names);
            } else {
                tHolder.owners.setText("לא שויך");
            }

            tHolder.checkBox.setOnCheckedChangeListener(null);
            tHolder.checkBox.setChecked(task.isDone());

            tHolder.checkBox.setOnCheckedChangeListener((b, isChecked) -> {
                FirebaseDatabase.getInstance()
                        .getReference("families")
                        .child(familyCode)
                        .child("tasks")
                        .child(task.getId())
                        .child("done")
                        .setValue(isChecked);
            });

            tHolder.delete.setOnClickListener(v -> {
                FirebaseDatabase.getInstance()
                        .getReference("families")
                        .child(familyCode)
                        .child("tasks")
                        .child(task.getId())
                        .removeValue();
            });

            // 4. הפעלת הלחיצה על כל השורה לצורך עריכה
            tHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            int[] colors = {0xE3F2FD, 0xE8F5E9, 0xFFF3E0, 0xF3E5F5, 0xFCE4EC, 0xE0F2F1};
            tHolder.card.setCardBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colors[position % colors.length]))));
        }
    }
    
    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textHeader;
        HeaderViewHolder(View v) {
            super(v);
            textHeader = v.findViewById(R.id.textCategoryHeader);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, owners;
        MaterialCheckBox checkBox;
        ImageButton delete;
        MaterialCardView card;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            owners = itemView.findViewById(R.id.textOwners);
            checkBox = itemView.findViewById(R.id.checkDone);
            delete = itemView.findViewById(R.id.btnDelete);
            card = itemView.findViewById(R.id.cardTask);
        }
    }
}
