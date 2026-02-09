package com.example.familywork;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText inputName, inputPhone, inputCode;
    private Button btnGenerate, btnEnter;

    private DatabaseReference familiesRef;
    private SharedPreferences prefs;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputName   = findViewById(R.id.inputName);
        inputPhone  = findViewById(R.id.inputPhone);
        inputCode   = findViewById(R.id.inputFamilyCode);
        btnGenerate = findViewById(R.id.btnGenerateCode);
        btnEnter    = findViewById(R.id.btnEnter);

        familiesRef = FirebaseDatabase.getInstance().getReference("families");
        prefs = getSharedPreferences("app", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        // כל משתמש מקבל UID (כניסה אנונימית)
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously();
        }

        btnGenerate.setOnClickListener(v -> createFamilyCode());
        btnEnter.setOnClickListener(v -> joinFamily());
    }

    // יצירת קוד משפחה
    private void createFamilyCode() {
        String code = randomCode(6);
        inputCode.setText(code);

        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "מלא שם וטלפון לפני יצירת קוד", Toast.LENGTH_SHORT).show();
            return;
        }

        addUserToFamily(code, name, phone);

        Toast.makeText(this, "קוד משפחה נוצר!", Toast.LENGTH_SHORT).show();
    }


    // כניסה למשפחה
    private void joinFamily() {
        String code = inputCode.getText().toString().trim();
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "מלא שם, טלפון וקוד משפחה", Toast.LENGTH_SHORT).show();
            return;
        }

        addUserToFamily(code, name, phone);

        prefs.edit().putString("familyCode", code).apply();

        startActivity(new Intent(this, StartActivity.class));
        finish();
    }


    // הוספת משתמש ל-info (מתוקן)
    private void addUserToFamily(String code, String name, String phone) {
        // 1. ניקוי מספר הטלפון מתווים שפיירבייס לא אוהב (כמו רווחים או מקפים)
        // נשתמש בזה בתור המזהה הייחודי של המשתמש
        String userId = phone.replaceAll("[^0-9]", "");

        DatabaseReference userRef = familiesRef
                .child(code)
                .child("info")
                .child(userId); // ⭐ שינוי: במקום push, אנחנו מצביעים על הטלפון הספציפי

        // 2. בדיקה אם המשתמש כבר קיים כדי לא לדרוס את תאריך ההצטרפות (joinedAt)
        userRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                // אם המשתמש לא קיים - ניצור אותו מחדש
                Map<String, Object> userData = new HashMap<>();
                userData.put("name", name);
                userData.put("phone", phone);
                userData.put("joinedAt", System.currentTimeMillis());
                userRef.setValue(userData);
            } else {
                // אם המשתמש כבר קיים - רק נעדכן את השם (למקרה שתיקן אותו)
                // לא ניגע ב-joinedAt ולא ניצור כפילות
                userRef.child("name").setValue(name);
            }
        });
    }


    // יצירת קוד אקראי
    private String randomCode(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}
