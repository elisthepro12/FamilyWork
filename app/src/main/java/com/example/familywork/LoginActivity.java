package com.example.familywork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputName, inputPhone, inputCode, inputFamilyName;
    private Button btnGenerate, btnEnter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("app", MODE_PRIVATE);

        // כניסה אוטומטית - רק אם יש נתונים מלאים
        boolean forceLogin = getIntent().getBooleanExtra("force_login", false);
        if (!forceLogin) {
            String savedPhone = prefs.getString("userPhone", "");
            Set<String> families = prefs.getStringSet("familyCodes", new HashSet<>());
            if (!savedPhone.isEmpty() && !families.isEmpty()) {
                startActivity(new Intent(this, StartActivity.class));
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_login);

        inputName = findViewById(R.id.inputName);
        inputPhone = findViewById(R.id.inputPhone);
        inputCode = findViewById(R.id.inputFamilyCode);
        inputFamilyName = findViewById(R.id.inputFamilyName);
        btnGenerate = findViewById(R.id.btnGenerateCode);
        btnEnter = findViewById(R.id.btnEnter);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously();
        }

        btnGenerate.setOnClickListener(v -> generateNewFamily());
        btnEnter.setOnClickListener(v -> joinExistingFamily());
    }

    private void generateNewFamily() {
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String familyName = inputFamilyName.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || familyName.isEmpty()) {
            Toast.makeText(this, "אנא מלא את שמך, טלפון ושם המשפחה", Toast.LENGTH_SHORT).show();
            return;
        }

        String newCode = randomCode(6);
        saveAndGo(newCode, familyName, name, phone);
    }

    private void joinExistingFamily() {
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String code = inputCode.getText().toString().trim().toUpperCase();

        if (name.isEmpty() || phone.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות להצטרפות", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("families").child(code).child("familyName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String fName = snapshot.getValue(String.class);
                        if (fName == null) fName = "משפחה ללא שם";
                        saveAndGo(code, fName, name, phone);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "שגיאה בחיבור", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveAndGo(String code, String fName, String userName, String userPhone) {
        String userId = userPhone.replaceAll("[^0-9]", "");
        if (userId.isEmpty()) return;

        DatabaseReference familyRef = FirebaseDatabase.getInstance().getReference("families").child(code);
        familyRef.child("familyName").setValue(fName);
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", userName);
        userData.put("phone", userPhone);
        familyRef.child("info").child(userId).updateChildren(userData);

        Set<String> familySet = new HashSet<>(prefs.getStringSet("familyCodes", new HashSet<>()));
        familySet.add(code + ":" + fName);

        prefs.edit()
                .putString("userPhone", userId)
                .putString("familyCode", code)
                .putStringSet("familyCodes", familySet)
                .apply();

        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String randomCode(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}
