package com.example.familywork;

import android.app.*;
import android.content.*;
import android.os.*;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.*;
import java.util.HashSet;
import java.util.Set;

public class TaskNotificationService extends Service {

    private String myPhone;
    private Set<String> familyStrings;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        myPhone = prefs.getString("userPhone", "");
        familyStrings = prefs.getStringSet("familyCodes", new HashSet<>());

        if (!myPhone.isEmpty() && !familyStrings.isEmpty()) {
            for (String entry : familyStrings) {
                // פיצול הקוד מהשם (פורמט code:name)
                String code = entry.split(":")[0];
                listenToFamily(code);
            }
        }
        return START_STICKY;
    }

    private void listenToFamily(String code) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("families").child(code).child("tasks");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String prev) {
                try {
                    Task task = snapshot.getValue(Task.class);
                    // בדיקה אם המשימה משויכת אלי
                    if (task != null && task.getOwners() != null && task.getOwners().containsKey(myPhone)) {
                        showNotification("משימה חדשה!", task.getTitle());
                    }
                } catch (Exception e) { }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot s, String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showNotification(String title, String content) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String cid = "family_tasks_channel";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(cid, "Family Tasks", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        Notification n = new NotificationCompat.Builder(this, cid)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        
        nm.notify((int)System.currentTimeMillis(), n);
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
