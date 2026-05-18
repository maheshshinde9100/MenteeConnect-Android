package com.mahesh.menteeconnect;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminMentorAllocationActivity extends AppCompatActivity {

    private TextView tvSelectedMentorName, tvMentorSlotsCapacity;
    private LinearLayout layoutSlotsDisplay;
    private CheckBox cbStudent1, cbStudent2, cbStudent3;

    // Mentor Capacity states
    private int selectedMentorIndex = -1; // -1 means none
    private final String[] mentorNames = {"Alice Williams", "John Doe", "Mahesh Shinde"};
    private final int[] maxMentees = {15, 15, 15};
    private final int[] utilizedMentees = {8, 14, 4}; // John has only 1 available slot!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_mentor_allocation);

        // System inset support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize general actions
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(view -> finish());

        // Bind layouts
        LinearLayout layoutSelect = findViewById(R.id.layout_select_mentor);
        tvSelectedMentorName = findViewById(R.id.tv_selected_mentor_name);
        tvMentorSlotsCapacity = findViewById(R.id.tv_mentor_slots_capacity);
        layoutSlotsDisplay = findViewById(R.id.layout_slots_display);

        cbStudent1 = findViewById(R.id.cb_student_1);
        cbStudent2 = findViewById(R.id.cb_student_2);
        cbStudent3 = findViewById(R.id.cb_student_3);
        Button btnAllocate = findViewById(R.id.btn_allocate);

        // Click to choose mentor
        layoutSelect.setOnClickListener(view -> triggerMentorSelection());

        // Pair allocator click
        btnAllocate.setOnClickListener(view -> {
            if (selectedMentorIndex == -1) {
                Toast.makeText(AdminMentorAllocationActivity.this, "Please choose a Faculty Advisor Mentor first!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate selected students count
            int selectedCount = 0;
            if (cbStudent1.isChecked() && cbStudent1.getVisibility() != View.GONE) selectedCount++;
            if (cbStudent2.isChecked() && cbStudent2.getVisibility() != View.GONE) selectedCount++;
            if (cbStudent3.isChecked() && cbStudent3.getVisibility() != View.GONE) selectedCount++;

            if (selectedCount == 0) {
                Toast.makeText(AdminMentorAllocationActivity.this, "Please choose at least one student to assign!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Read Mentor stats
            String mentorName = mentorNames[selectedMentorIndex];
            int max = maxMentees[selectedMentorIndex];
            int utilized = utilizedMentees[selectedMentorIndex];
            int available = max - utilized;

            // Capacity check validation
            if (selectedCount > available) {
                new AlertDialog.Builder(AdminMentorAllocationActivity.this)
                        .setTitle("Capacity Limit Overload")
                        .setMessage("Warning: You are attempting to assign " + selectedCount + " students to " + mentorName +
                                ", but they only have " + available + " slot(s) remaining. Please select fewer students or choose another mentor.")
                        .setPositiveButton("Configure Options", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }

            // Allocate successfully!
            utilizedMentees[selectedMentorIndex] += selectedCount;
            int newUtilized = utilizedMentees[selectedMentorIndex];
            int newAvailable = max - newUtilized;

            // Update display slots
            tvMentorSlotsCapacity.setText(newUtilized + " / " + max + " Utilized (" + newAvailable + " available)");

            // Uncheck and simulated hide allocated students
            if (cbStudent1.isChecked()) {
                cbStudent1.setChecked(false);
                cbStudent1.setVisibility(View.GONE);
            }
            if (cbStudent2.isChecked()) {
                cbStudent2.setChecked(false);
                cbStudent2.setVisibility(View.GONE);
            }
            if (cbStudent3.isChecked()) {
                cbStudent3.setChecked(false);
                cbStudent3.setVisibility(View.GONE);
            }

            Toast.makeText(AdminMentorAllocationActivity.this, "Successfully allocated " + selectedCount + " student(s) to " + mentorName + "!", Toast.LENGTH_LONG).show();
        });
    }

    // Modal select list
    private void triggerMentorSelection() {
        String[] options = new String[mentorNames.length];
        for (int i = 0; i < mentorNames.length; i++) {
            int available = maxMentees[i] - utilizedMentees[i];
            options[i] = mentorNames[i] + " (" + available + " slots free)";
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Mentor")
                .setItems(options, (dialog, index) -> {
                    selectedMentorIndex = index;
                    String mentorName = mentorNames[index];
                    tvSelectedMentorName.setText(mentorName);

                    int max = maxMentees[index];
                    int utilized = utilizedMentees[index];
                    int available = max - utilized;

                    tvMentorSlotsCapacity.setText(utilized + " / " + max + " Utilized (" + available + " available)");
                    layoutSlotsDisplay.setVisibility(View.VISIBLE);

                    // Re-style slot text based on capacity warnings
                    if (available <= 1) {
                        tvMentorSlotsCapacity.setTextColor(getColor(R.color.google_red));
                    } else if (available <= 5) {
                        tvMentorSlotsCapacity.setTextColor(getColor(R.color.accent_orange));
                    } else {
                        tvMentorSlotsCapacity.setTextColor(getColor(R.color.accent_green));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
