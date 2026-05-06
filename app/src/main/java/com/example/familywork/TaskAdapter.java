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
import java.util.List;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private String familyCode;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    private OnTaskClickListener listener;

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public TaskAdapter(List<Task> tasks, String familyCode) {
        this.tasks = tasks;
        this.familyCode = familyCode;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.title.setText(task.getTitle());

        if (task.getOwners() != null && !task.getOwners().isEmpty()) {
            StringBuilder names = new StringBuilder();
            for (Map.Entry<String, String> e : task.getOwners().entrySet()) {
                names.append(e.getValue()).append(" ");
            }
            holder.owners.setText("אחראי: " + names);
        } else {
            holder.owners.setText("לא שויך");
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone());

        holder.checkBox.setOnCheckedChangeListener((b, isChecked) -> {
            FirebaseDatabase.getInstance()
                    .getReference("families")
                    .child(familyCode)
                    .child("tasks")
                    .child(task.getId())
                    .child("done")
                    .setValue(isChecked);
        });

        holder.delete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Task t = tasks.get(pos);
            FirebaseDatabase.getInstance()
                    .getReference("families")
                    .child(familyCode)
                    .child("tasks")
                    .child(t.getId())
                    .removeValue();
            tasks.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, tasks.size());
        });

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
        }

        // צבעים פסטליים עדינים למראה יוקרתי (High Level)
        int[] colors = {
                Color.parseColor("#E3F2FD"), // כחול עדין
                Color.parseColor("#E8F5E9"), // ירוק עדין
                Color.parseColor("#FFF3E0"), // כתום עדין
                Color.parseColor("#F3E5F5"), // סגול עדין
                Color.parseColor("#FCE4EC"), // ורוד עדין
                Color.parseColor("#E0F2F1")  // טורקיז עדין
        };

        holder.card.setCardBackgroundColor(colors[position % colors.length]);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView owners;
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
