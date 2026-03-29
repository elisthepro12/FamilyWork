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
    private Set<String> familyCodes;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        myPhone = prefs.getString("userPhone", "");
        familyCodes = prefs.getStringSet("familyCodes", new HashSet<>());

        if (!myPhone.isEmpty() && !familyCodes.isEmpty()) {
            for (String code : familyCodes) {
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
                    if (task != null && task.getOwners() != null && task.getOwners().containsKey(myPhone)) {
                        showNotification(task.getTitle(), "משפחה: " + code);
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
        String cid = "multi_fam_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(cid, "Family Tasks", NotificationManager.IMPORTANCE_HIGH));
        }
        Notification n = new NotificationCompat.Builder(this, cid)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(content)
                .setContentText(title)
                .setAutoCancel(true)
                .build();
        nm.notify((int)System.currentTimeMillis(), n);
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}