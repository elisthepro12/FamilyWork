package com.example.familywork;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// זהו המסך הראשי של האפליקציה שמנהל את הניווט בין המשימות, הקניות וההיסטוריה
public class StartActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private final String ADD_NEW_FAMILY = "+ הוסף משפחה";
    private boolean isFirstSelection = true; // משתנה עזר למניעת רענון מיותר כשהמסך רק עולה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        prefs = getSharedPreferences("app", MODE_PRIVATE);

        // הפעלת שירות ההתראות שמאזין לשינויים ב-Firebase בזמן אמת
        startService(new Intent(this, TaskNotificationService.class));
        
        // הגדרת התזכורת היומית (Alarm) שתקפוץ בכל בוקר
        setDailyAlarm();

        // אתחול התפריט העליון לבחירת משפחה
        setupFamilySpinner();

        // הגדרת סרגל הניווט התחתון (Bottom Navigation)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        // טעינת דף המשימות כברירת מחדל עם פתיחת האפליקציה
        if (savedInstanceState == null) {
            loadFragment(new fragment_tasks());
        }

        // הגדרת המעבר בין הפרגמנטים השונים לפי בחירת המשתמש בתפריט התחתון
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();
            if (id == R.id.nav_tasks) f = new fragment_tasks();
            else if (id == R.id.nav_inventory) f = new ShoppingListFragment();
            else if (id == R.id.nav_history) f = new HistoryFragment();
            
            if (f != null) {
                loadFragment(f);
                return true;
            }
            return false;
        });
    }

    // פונקציה שמתזמנת התראה יומית בשעה 08:00 בבוקר לביצוע משימות
    private void setDailyAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // קביעת השעה לשמונה בבוקר
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 22);
        calendar.set(Calendar.SECOND, 0);

        // אם השעה הנוכחית כבר אחרי שמונה, נתזמן למחר בבוקר
        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager == null) return;

        // טיפול בהתראות מדויקות לפי דרישות הגרסה של אנדרואיד (12 ומעלה)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                // ללא הרשאה לאלרם מדויק - שימוש בחלון זמן גמיש
                alarmManager.setWindow(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        15 * 60 * 1000, pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    // הגדרת ה-Spinner (התפריט הנפתח) להחלפת קבוצות משפחה
    private void setupFamilySpinner() {
        Spinner spinner = findViewById(R.id.familySpinner);
        if (spinner == null) return;
        
        // שליפת כל הקודים של המשפחות שהמשתמש חבר בהן
        Set<String> familySet = prefs.getStringSet("familyCodes", new HashSet<>());
        List<String> families = new ArrayList<>(familySet);
        families.add(ADD_NEW_FAMILY); // אפשרות להוספת משפחה חדשה
        
        // יצירת המתאם לעיצוב התפריט (שימוש ב-spinner_item המותאם אישית)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, families);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        // בחירת המשפחה הנוכחית השמורה בזיכרון
        String current = prefs.getString("familyCode", "");
        int currentIndex = families.indexOf(current);
        if (currentIndex != -1) spinner.setSelection(currentIndex);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // מניעת הפעלה כפולה בטעינה הראשונה של הדף
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;
                }

                String selected = families.get(position);
                if (selected.equals(ADD_NEW_FAMILY)) {
                    // מעבר למסך הכניסה להוספת משפחה נוספת
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    intent.putExtra("force_login", true);
                    startActivity(intent);
                } else if (!selected.equals(prefs.getString("familyCode", ""))) {
                    // עדכון המשפחה הנבחרת בזיכרון וריענון המסך כדי להציג את הנתונים החדשים
                    prefs.edit().putString("familyCode", selected).apply();
                    recreate(); 
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // פונקציה שמחליפה את התוכן במרכז המסך (Fragment) בצורה חלקה
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
