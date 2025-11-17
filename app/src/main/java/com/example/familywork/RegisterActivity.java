package com.example.familywork; // שנה לפי החבילה שלך

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, familyInput;
    private Button registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        familyInput = findViewById(R.id.familyInput);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String family = familyInput.getText().toString().trim();

                if(email.isEmpty() || password.isEmpty() || family.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return;
                }

                // יצירת משתמש ב-Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(user != null) {
                                    String uid = user.getUid();

                                    // שמירת המידע ב-Firebase Database
                                    FirebaseDatabase.getInstance().getReference("users")
                                            .child(uid)
                                            .setValue(new User(email, family, uid))
                                            .addOnCompleteListener(dbTask -> {
                                                if(dbTask.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, "המשתמש נרשם בהצלחה", Toast.LENGTH_SHORT).show();
                                                    // מעבר ל-Login או MainActivity
                                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, "שגיאה בבסיס הנתונים: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "שגיאה בהרשמה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        Button registerBtn = findViewById(R.id.loginButton); // צור כפתור ב-XML
        registerBtn.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    // מחלקת User (מודל)
    public static class User {
        public String email;
        public String family;
        public String uid;

        public User() {} // דרוש על ידי Firebase

        public User(String email, String family, String uid) {
            this.email = email;
            this.family = family;
            this.uid = uid;
        }
    }
}
