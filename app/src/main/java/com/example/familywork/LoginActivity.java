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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private EditText inputName, inputPhone, inputCode;
    private Button btnGenerate, btnEnter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("app", MODE_PRIVATE);

        // בדיקה האם הגענו לפה בלחיצה על "הוסף משפחה"
        boolean forceLogin = getIntent().getBooleanExtra("force_login", false);

        // כניסה אוטומטית רק אם לא ביקשו "כניסה בכוח"
        if (!forceLogin) {
            if (!prefs.getString("userPhone", "").isEmpty() && !prefs.getStringSet("familyCodes", new HashSet<>()).isEmpty()) {
                startActivity(new Intent(this, StartActivity.class));
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_login);

        inputName = findViewById(R.id.inputName);
        inputPhone = findViewById(R.id.inputPhone);
        inputCode = findViewById(R.id.inputFamilyCode);
        btnGenerate = findViewById(R.id.btnGenerateCode);
        btnEnter = findViewById(R.id.btnEnter);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously();
        }

        btnGenerate.setOnClickListener(v -> handleFamilyAction(true));
        btnEnter.setOnClickListener(v -> handleFamilyAction(false));
    }

    private void handleFamilyAction(boolean isNew) {
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String code = isNew ? randomCode(6) : inputCode.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(code)) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = phone.replaceAll("[^0-9]", "");

        Set<String> familySet = new HashSet<>(prefs.getStringSet("familyCodes", new HashSet<>()));
        familySet.add(code);

        prefs.edit()
                .putString("userPhone", userId)
                .putString("familyCode", code)
                .putStringSet("familyCodes", familySet)
                .apply();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("families").child(code).child("info").child(userId);
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);
        userRef.updateChildren(userData);

        startActivity(new Intent(this, StartActivity.class));
        finish();
    }

    private String randomCode(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}