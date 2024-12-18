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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        TextView registerLink = findViewById(R.id.registerLink);

        loginButton.setOnClickListener(v -> handleLogin());
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, go to welcome page
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
            finish();
        }
    }

    private void handleLogin() {
        if (loginButton != null) {
            loginButton.setEnabled(false);
        }

        try {
            String email = emailInput != null ? emailInput.getText().toString().trim() : "";
            String password = passwordInput != null ? passwordInput.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(email, password);

        } catch (Exception e) {
            Log.e(TAG, "Error in handleLogin: " + e.getMessage(), e);
            Toast.makeText(this, "Error processing login: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (loginButton != null) {
                loginButton.setEnabled(true);
            }
        }
    }

    private void performLogin(String email, String password) {
        try {
            if (mAuth == null) {
                Log.e(TAG, "FirebaseAuth not initialized");
                Toast.makeText(this, "Error: Authentication not initialized", Toast.LENGTH_LONG).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Login successful");
                            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
                            finish();
                        } else {
                            Log.w(TAG, "Login failed", task.getException());
                            String errorMessage = task.getException() != null ? 
                                    task.getException().getMessage() : 
                                    "Authentication failed";
                            Toast.makeText(LoginActivity.this, 
                                "Login failed: " + errorMessage, 
                                Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in performLogin: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
