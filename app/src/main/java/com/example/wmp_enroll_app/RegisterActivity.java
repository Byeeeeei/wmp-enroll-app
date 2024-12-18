package com.example.wmp_enroll_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private TextInputEditText fullNameInput, emailInput, passwordInput;
    private MaterialButton registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        fullNameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);
        TextView loginLink = findViewById(R.id.loginLink);

        registerButton.setOnClickListener(v -> handleRegistration());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegistration() {
        registerButton.setEnabled(false);

        try {
            String fullName = fullNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(fullName)) {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            performRegistration(fullName, email, password);

        } catch (Exception e) {
            Log.e(TAG, "Error in handleRegistration: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            registerButton.setEnabled(true);
        }
    }

    private void performRegistration(String fullName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Set display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                            Toast.makeText(RegisterActivity.this,
                                                    "Registration successful!",
                                                    Toast.LENGTH_SHORT).show();
                                            // Navigate to welcome screen
                                            startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class));
                                            finish();
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Registration failed";
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}