package com.example.wmp_enroll_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView userNameText, userEmailText;
    private MaterialButton logoutButton, enrollButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        logoutButton = findViewById(R.id.logoutButton);
        enrollButton = findViewById(R.id.enrollButton);

        // Set up logout button
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            finish();
        });

        // Set up enroll button
        enrollButton.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, EnrollmentActivity.class));
        });

        // Display user info
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String displayName = user.getDisplayName();
            
            if (displayName != null && !displayName.isEmpty()) {
                userNameText.setText(displayName);
            }
            
            if (email != null && !email.isEmpty()) {
                userEmailText.setText(email);
            }
        } else {
            // If no user is signed in, go back to login
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If no user is signed in, go back to login
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            finish();
        }
    }
}
