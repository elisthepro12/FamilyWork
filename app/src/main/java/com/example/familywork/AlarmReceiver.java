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

// רכיב ה-Receiver שמתעורר בזמן שהוגדר ב-AlarmManager (בכל בוקר ב-08:00)
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // משיכת נתוני המשתמש מהזיכרון כדי לדעת לאילו משפחות להאזין
        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);
        String myPhone = prefs.getString("userPhone", "");
        Set<String> familyStrings = prefs.getStringSet("familyCodes", new HashSet<>());

        if (myPhone.isEmpty() || familyStrings.isEmpty()) return;

        // מעבר על כל המשפחות שהמשתמש חבר בהן ב-Firebase
        for (String entry : familyStrings) {
            // פיצול הקוד מהשם (פורמט code:name) - קריטי כדי למצוא את הנתיב ב-Firebase
            String code = entry.split(":")[0];
            
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("families").child(code).child("tasks");

            // בדיקה חד פעמית של מצב המשימות בענן
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean foundTask = false;
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Task task = snap.getValue(Task.class);
                        // בדיקת לוגיקה: האם המשימה יומית, לא בוצעה, ושייכת למשתמש הנוכחי?
                        if (task != null && task.isDaily() && !task.isDone()) {
                            if (task.getOwners() != null && task.getOwners().containsKey(myPhone)) {
                                foundTask = true;
                                break;
                            }
                        }
                    }
                    // אם נמצאה משימה שמחכה לטיפול, נקפיץ התראה לטלפון
                    if (foundTask) {
                        showNotification(context, "תזכורת יומית", "יש לך משימות יומיות שמחכות לביצוע!");
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // בנייה והצגה של ההתראה בשורת המשימות של המכשיר
    private void showNotification(Context context, String title, String text) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "daily_tasks_channel";

        // יצירת ערוץ התראות (חובה באנדרואיד 8 ומעלה)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Daily Reminders", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        // הגדרת הפעולה שתקרה בלחיצה על ההתראה - פתיחת האפליקציה
        Intent openAppIntent = new Intent(context, StartActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        // שליחת ההתראה בפועל
        nm.notify(999, builder.build());
    }
}
