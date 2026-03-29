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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StartActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private final String ADD_NEW_FAMILY = "+ הוסף משפחה";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        prefs = getSharedPreferences("app", MODE_PRIVATE);

        // הפעלת שירות ההתראות המיידיות
        startService(new Intent(this, TaskNotificationService.class));

        // הפעלת האלרם היומי
        setDailyAlarm();

        setupFamilySpinner();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (savedInstanceState == null) {
            loadFragment(new fragment_tasks());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();
            if (id == R.id.nav_tasks) f = new fragment_tasks();
            else if (id == R.id.nav_inventory) f = new ShoppingListFragment();
            else if (id == R.id.nav_history) f = new HistoryFragment();
            if (f != null) loadFragment(f);
            return true;
        });
    }

    private void setDailyAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8); // שעה 8 בבוקר
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void setupFamilySpinner() {
        Spinner spinner = findViewById(R.id.familySpinner);
        if (spinner == null) return;
        Set<String> familySet = prefs.getStringSet("familyCodes", new HashSet<>());
        List<String> families = new ArrayList<>(familySet);
        families.add(ADD_NEW_FAMILY);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, families);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        String current = prefs.getString("familyCode", "");
        int currentIndex = families.indexOf(current);
        if (currentIndex != -1) spinner.setSelection(currentIndex);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = families.get(position);
                if (selected.equals(ADD_NEW_FAMILY)) {
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    intent.putExtra("force_login", true);
                    startActivity(intent);
                } else if (!selected.equals(prefs.getString("familyCode", ""))) {
                    prefs.edit().putString("familyCode", selected).apply();
                    recreate();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}