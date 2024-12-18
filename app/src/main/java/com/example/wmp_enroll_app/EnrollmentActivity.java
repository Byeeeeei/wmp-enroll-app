package com.example.wmp_enroll_app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.wmp_enroll_app.models.Subject;
import com.example.wmp_enroll_app.models.Enrollment;
import com.example.wmp_enroll_app.adapters.SubjectAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentActivity extends AppCompatActivity {
    private static final int MAX_CREDITS = 24;
    private RecyclerView subjectsRecyclerView;
    private MaterialButton enrollButton;
    private TextView creditsInfoText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Subject> subjects;
    private List<Subject> selectedSubjects;
    private int totalCredits = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        subjectsRecyclerView = findViewById(R.id.subjectsRecyclerView);
        enrollButton = findViewById(R.id.enrollButton);
        creditsInfoText = findViewById(R.id.creditsInfoText);

        // Initialize lists
        subjects = new ArrayList<>();
        selectedSubjects = new ArrayList<>();

        // Set up RecyclerView
        subjectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Check if user already enrolled
        checkExistingEnrollment();

        // Load subjects
        loadSubjects();

        // Set up enrollment button
        enrollButton.setOnClickListener(v -> handleEnrollment());
    }

    private void checkExistingEnrollment() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("enrollments")
                    .whereEqualTo("userId", currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // User already enrolled, show summary instead
                            showEnrollmentSummary(queryDocumentSnapshots.getDocuments().get(0).toObject(Enrollment.class));
                        }
                    });
        }
    }

    private void loadSubjects() {
        // Add subjects with their credits
        subjects.add(new Subject("MC101", "Microcontroller", 3, "Embedded Systems"));
        subjects.add(new Subject("IOT101", "IoT Programming", 3, "IoT"));
        subjects.add(new Subject("ES101", "Embedded System", 4, "Embedded Systems"));
        subjects.add(new Subject("ROB101", "Robotics", 4, "Robotics"));
        subjects.add(new Subject("ANS101", "Automatics Navigation System", 3, "Robotics"));
        subjects.add(new Subject("IOTP101", "IoT Project", 4, "IoT"));
        subjects.add(new Subject("CSF101", "Cyber Security Fundamentals", 3, "Security"));
        subjects.add(new Subject("SRM101", "Security Risk Management", 3, "Security"));
        subjects.add(new Subject("SCA101", "Security Compliance and Audit", 3, "Security"));
        subjects.add(new Subject("EH101", "Ethical Hacking", 4, "Security"));
        subjects.add(new Subject("DF101", "Digital Forensics", 3, "Security"));
        subjects.add(new Subject("CSP101", "Cyber Security Project", 4, "Security"));
        subjects.add(new Subject("IPR101", "Image Processing and Recognition", 3, "AI"));
        subjects.add(new Subject("CV101", "Computer Vision", 4, "AI"));
        subjects.add(new Subject("NLP101", "Natural Language Processing", 4, "AI"));
        subjects.add(new Subject("NLUG101", "Natural Language Understanding and Generation", 3, "AI"));
        subjects.add(new Subject("IR101", "Intelligent Robotics", 4, "AI"));
        subjects.add(new Subject("DL101", "Deep Learning", 4, "AI"));

        // Set up adapter
        SubjectAdapter adapter = new SubjectAdapter(subjects, (subject, isChecked) -> {
            if (isChecked) {
                if (totalCredits + subject.getCredits() <= MAX_CREDITS) {
                    selectedSubjects.add(subject);
                    totalCredits += subject.getCredits();
                } else {
                    Toast.makeText(this, "Maximum credits (24) exceeded!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                selectedSubjects.remove(subject);
                totalCredits -= subject.getCredits();
            }
            updateCreditsInfo();
            return true;
        });
        
        subjectsRecyclerView.setAdapter(adapter);
    }

    private void updateCreditsInfo() {
        creditsInfoText.setText(String.format("Selected Credits: %d/%d", totalCredits, MAX_CREDITS));
    }

    private void handleEnrollment() {
        if (selectedSubjects.isEmpty()) {
            Toast.makeText(this, "Please select at least one subject", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create enrollment object
        List<String> subjectIds = new ArrayList<>();
        for (Subject subject : selectedSubjects) {
            subjectIds.add(subject.getId());
        }

        Enrollment enrollment = new Enrollment(
                currentUser.getUid(),
                subjectIds,
                totalCredits
        );

        // Save to Firestore
        db.collection("enrollments")
                .document(currentUser.getUid())
                .set(enrollment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Enrollment successful!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Enrollment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEnrollmentSummary(Enrollment enrollment) {
        // Hide the selection UI and show summary instead
        // You might want to create a separate layout for this
        enrollButton.setEnabled(false);
        
        // Filter subjects to show only enrolled ones
        List<Subject> enrolledSubjects = new ArrayList<>();
        for (Subject subject : subjects) {
            if (enrollment.getEnrolledSubjects().contains(subject.getId())) {
                enrolledSubjects.add(subject);
            }
        }

        // Update adapter with enrolled subjects only
        SubjectAdapter adapter = new SubjectAdapter(enrolledSubjects, null);
        subjectsRecyclerView.setAdapter(adapter);

        // Update credits info
        creditsInfoText.setText(String.format("Total Enrolled Credits: %d", enrollment.getTotalCredits()));
        
        // Update title
        TextView titleText = findViewById(R.id.titleText);
        titleText.setText("Enrollment Summary");
    }
}
