package com.mahesh.menteeconnect;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;

public class AdminBulkUploadActivity extends AppCompatActivity {

    private MaterialCardView cardUploadZone, cardFileStatus;
    private TextView tvFileName, tvConsoleLogs;
    private Button btnImport;

    private final Handler handler = new Handler();
    private int logStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_bulk_upload);

        // System inset support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize general actions
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(view -> finish());

        // Bind layout views
        cardUploadZone = findViewById(R.id.card_upload_zone);
        cardFileStatus = findViewById(R.id.card_file_status);
        tvFileName = findViewById(R.id.tv_selected_file_name);
        tvConsoleLogs = findViewById(R.id.tv_console_logs);
        btnImport = findViewById(R.id.btn_import);

        // Mock upload zone click
        cardUploadZone.setOnClickListener(view -> triggerMockFileSelection());

        // Import execute click
        btnImport.setOnClickListener(view -> beginSimulatedImport());
    }

    // Modal file selection popup
    private void triggerMockFileSelection() {
        String[] mockFiles = {
                "student_mentee_seeding_2026.csv (14.2 KB)",
                "mentors_roster_departments.csv (8.6 KB)",
                "mentee_connect_demo_dataset.csv (32.1 KB)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select CSV File Target")
                .setItems(mockFiles, (dialog, index) -> {
                    String selected = mockFiles[index];
                    tvFileName.setText(selected);
                    cardFileStatus.setVisibility(View.VISIBLE);

                    tvConsoleLogs.setText("Console Active.\n[INFO] Selected CSV file target: " + selected.split(" \\(")[0] + "\nReady to parse and upload.\n");
                    Toast.makeText(AdminBulkUploadActivity.this, "Spreadsheet matched successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Delayed console lines injection
    private void beginSimulatedImport() {
        btnImport.setEnabled(false);
        cardUploadZone.setEnabled(false);
        tvConsoleLogs.append("\n[START] Parsing CSV stream nodes...\n");

        logStep = 0;
        runSeedingTicks();
    }

    private void runSeedingTicks() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (logStep) {
                    case 0:
                        tvConsoleLogs.append("[INFO] Fetching headers schema mapping... [firstName, lastName, email, course, semester, batch]\n");
                        logStep++;
                        runSeedingTicks();
                        break;
                    case 1:
                        tvConsoleLogs.append("[INFO] Row 1: Parsed Mentee 'Priyanka Shinde' successfully.\n");
                        logStep++;
                        runSeedingTicks();
                        break;
                    case 2:
                        tvConsoleLogs.append("[INFO] Row 2: Parsed Mentee 'Rohan Dev' successfully.\n");
                        logStep++;
                        runSeedingTicks();
                        break;
                    case 3:
                        tvConsoleLogs.append("[INFO] Hashing security credentials with BCrypt... Done.\n");
                        logStep++;
                        runSeedingTicks();
                        break;
                    case 4:
                        tvConsoleLogs.append("[INFO] Syncing connections logic for cohort CS 2026... Done.\n");
                        logStep++;
                        runSeedingTicks();
                        break;
                    case 5:
                        tvConsoleLogs.append("[SUCCESS] Seeding operations compiled successfully!\n[STATUS] MongoDB DBRef nodes mapped correctly.\n");
                        Toast.makeText(AdminBulkUploadActivity.this, "Seeding complete! 2 records imported successfully.", Toast.LENGTH_LONG).show();
                        btnImport.setEnabled(true);
                        cardUploadZone.setEnabled(true);
                        break;
                }
            }
        }, 1000); // 1-second delay increments
    }
}
