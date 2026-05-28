package com.example.familywork;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.HashSet;
import android.Manifest;

import android.content.pm.PackageManager;
import android.os.Build;

public class MainActivity extends AppCompatActivity {

    private Button enterButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // בדיקה אם המכשיר הוא אנדרואיד 13 ומעלה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // בדיקה אם המשתמש כבר נתן אישור
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // אם לא - מבקשים ממנו
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 1. בדיקת חיבור אוטומטי לפני טעינת המסך
        prefs = getSharedPreferences("app", MODE_PRIVATE);
        String savedPhone = prefs.getString("userPhone", "");
        
        if (!savedPhone.isEmpty()) {
            // אם המשתמש כבר מחובר, מדלגים ישר ל-LoginActivity (שמטפל בכניסת Firebase)
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // 2. טעינת המסך רק אם המשתמש לא מחובר
        setContentView(R.layout.activity_main);

        enterButton = findViewById(R.id.enterButton);

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}
