package com.example.familywork;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.*;
import java.util.HashSet;
import java.util.Set;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);
        String myPhone = prefs.getString("userPhone", "");
        Set<String> familyCodes = prefs.getStringSet("familyCodes", new HashSet<>());

        if (myPhone.isEmpty() || familyCodes.isEmpty()) return;

        // עוברים על כל המשפחות שהמשתמש רשום אליהן
        for (String code : familyCodes) {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("families").child(code).child("tasks");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean foundTask = false;
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Task task = snap.getValue(Task.class);
                        // בדיקה: האם המשימה יומית, לא בוצעה, ושייכת לי?
                        if (task != null && task.isDaily() && !task.isDone()) {
                            if (task.getOwners() != null && task.getOwners().containsKey(myPhone)) {
                                foundTask = true;
                                break;
                            }
                        }
                    }
                    if (foundTask) {
                        showNotification(context, "תזכורת יומית", "יש לך משימות יומיות שמחכות לביצוע!");
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void showNotification(Context context, String title, String text) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "daily_tasks_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Daily Reminders", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        Intent openAppIntent = new Intent(context, StartActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        nm.notify(999, builder.build());
    }
}