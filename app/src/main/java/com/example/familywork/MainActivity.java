package com.example.familywork;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private Button enterButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
