package com.mahesh.menteeconnect.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;
import com.mahesh.menteeconnect.R;

public class AdminBatchManagementActivity extends AppCompatActivity {

    private EditText etName, etYear, etDept;
    private LinearLayout layoutBatchesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_batch_management);

        // System inset bindings
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize general actions
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(view -> finish());

        // Initialize creation fields
        etName = findViewById(R.id.et_batch_name);
        etYear = findViewById(R.id.et_batch_year);
        etDept = findViewById(R.id.et_batch_dept);
        Button btnCreate = findViewById(R.id.btn_create_cohort);
        layoutBatchesList = findViewById(R.id.layout_batches_list);

        // Configure static first card actions
        TextView tvAssigned1 = findViewById(R.id.tv_allocated_mentors_count_1);
        Button btnAssign1 = findViewById(R.id.btn_assign_mentor_1);
        btnAssign1.setOnClickListener(view -> triggerMentorSelection(tvAssigned1));

        // Create Cohort dynamic click
        btnCreate.setOnClickListener(view -> {
            String name = etName.getText().toString().trim();
            String year = etYear.getText().toString().trim();
            String dept = etDept.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(year) || TextUtils.isEmpty(dept)) {
                Toast.makeText(AdminBatchManagementActivity.this, "Please fulfill all cohort descriptors.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Dynamically inflate and insert new Batch Card into container list
            spawnBatchCard(name, year, dept);

            // Clean inputs
            etName.setText("");
            etYear.setText("");
            etDept.setText("");

            Toast.makeText(AdminBatchManagementActivity.this, "Cohort " + name + " established successfully!", Toast.LENGTH_SHORT).show();
        });
    }

    // Dynamic cohort items generator
    private void spawnBatchCard(String name, String year, String dept) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 32);
        card.setLayoutParams(cardParams);
        card.setCardElevation(4);
        card.setRadius(32);
        card.setStrokeWidth(2);
        card.setStrokeColor(getColor(R.color.border_light));
        card.setCardBackgroundColor(getColorStateList(R.color.white));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 32, 32, 32);
        card.addView(container);

        // Sub Header Row
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        container.addView(header);

        // Icon
        android.widget.ImageView icon = new android.widget.ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
        icon.setImageResource(R.drawable.ic_batch);
        header.addView(icon);

        // Details Layout
        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        detailsParams.setMarginStart(24);
        details.setLayoutParams(detailsParams);
        header.addView(details);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(name);
        tvTitle.setTextColor(getColor(R.color.text_primary));
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        details.addView(tvTitle);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("Year: " + year + " | Dept: " + dept);
        tvSubtitle.setTextColor(getColor(R.color.text_secondary));
        tvSubtitle.setTextSize(12);
        tvSubtitle.setPadding(0, 4, 0, 0);
        details.addView(tvSubtitle);

        // Separator
        View sep = new View(this);
        LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2
        );
        sepParams.setMargins(0, 24, 0, 24);
        sep.setLayoutParams(sepParams);
        sep.setBackgroundColor(getColor(R.color.border_light));
        container.addView(sep);

        // Actions Row
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(android.view.Gravity.CENTER_VERTICAL);
        container.addView(actions);

        // Stat 1 (Mentees)
        TextView badgeStudents = new TextView(this);
        badgeStudents.setText("0 Mentees");
        badgeStudents.setTextSize(11);
        badgeStudents.setTypeface(null, android.graphics.Typeface.BOLD);
        badgeStudents.setTextColor(getColor(R.color.primary));
        badgeStudents.setBackgroundResource(R.drawable.bg_social_button);
        badgeStudents.setBackgroundTintList(getColorStateList(R.color.primary_light));
        badgeStudents.setPadding(20, 8, 20, 8);
        actions.addView(badgeStudents);

        // Stat 2 (Mentor)
        TextView badgeMentor = new TextView(this);
        badgeMentor.setText("Mentor: Unassigned");
        badgeMentor.setTextSize(11);
        badgeMentor.setTypeface(null, android.graphics.Typeface.BOLD);
        badgeMentor.setTextColor(getColor(R.color.accent_orange));
        badgeMentor.setBackgroundResource(R.drawable.bg_social_button);
        badgeMentor.setBackgroundTintList(getColorStateList(R.color.accent_orange_light));
        badgeMentor.setPadding(20, 8, 20, 8);
        LinearLayout.LayoutParams mentorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mentorParams.setMarginStart(16);
        badgeMentor.setLayoutParams(mentorParams);
        actions.addView(badgeMentor);

        // Spacing weight
        View space = new View(this);
        actions.addView(space, new LinearLayout.LayoutParams(0, 1, 1));

        // Assign Button
        com.google.android.material.button.MaterialButton btnAssign = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
        btnAssign.setText("Assign");
        btnAssign.setAllCaps(false);
        btnAssign.setTextSize(12);
        btnAssign.setOnClickListener(view -> triggerMentorSelection(badgeMentor));
        actions.addView(btnAssign);

        // Insert at index 0 (top of the lists)
        layoutBatchesList.addView(card, 0);
    }

    // Modal dialog displaying roster of mentors to assign
    private void triggerMentorSelection(TextView tvMentorDisplay) {
        String[] mentors = {"Alice Williams (Information Tech)", "John Doe (Computer Eng)", "Mahesh Shinde (Data Systems)"};

        new AlertDialog.Builder(this)
                .setTitle("Select Advisor Faculty")
                .setItems(mentors, (dialog, index) -> {
                    String selected = mentors[index];
                    String name = selected.split(" \\(")[0];
                    tvMentorDisplay.setText("Mentor: " + name);
                    Toast.makeText(AdminBatchManagementActivity.this, name + " allocated to batch.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
