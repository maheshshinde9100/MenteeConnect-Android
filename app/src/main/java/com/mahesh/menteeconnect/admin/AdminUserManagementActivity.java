package com.mahesh.menteeconnect.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;
import com.mahesh.menteeconnect.R;

public class AdminUserManagementActivity extends AppCompatActivity {

    // Views for User 1
    private MaterialCardView cardUser1;
    private TextView tvName1, tvEmail1, tvRole1;

    // Views for User 2
    private MaterialCardView cardUser2;
    private TextView tvName2, tvEmail2, tvRole2;

    // Views for User 3
    private MaterialCardView cardUser3;
    private TextView tvName3, tvEmail3, tvRole3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_user_management);

        // System insets support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize general actions
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(view -> finish());

        // Bind User 1 elements
        cardUser1 = findViewById(R.id.card_user_1);
        tvName1 = findViewById(R.id.tv_user_name_1);
        tvEmail1 = findViewById(R.id.tv_user_email_1);
        tvRole1 = findViewById(R.id.tv_user_role_1);
        ImageButton btnEdit1 = findViewById(R.id.btn_edit_user_1);
        ImageButton btnDelete1 = findViewById(R.id.btn_delete_user_1);

        // Bind User 2 elements
        cardUser2 = findViewById(R.id.card_user_2);
        tvName2 = findViewById(R.id.tv_user_name_2);
        tvEmail2 = findViewById(R.id.tv_user_email_2);
        tvRole2 = findViewById(R.id.tv_user_role_2);
        ImageButton btnEdit2 = findViewById(R.id.btn_edit_user_2);
        ImageButton btnDelete2 = findViewById(R.id.btn_delete_user_2);

        // Bind User 3 elements
        cardUser3 = findViewById(R.id.card_user_3);
        tvName3 = findViewById(R.id.tv_user_name_3);
        tvEmail3 = findViewById(R.id.tv_user_email_3);
        tvRole3 = findViewById(R.id.tv_user_role_3);
        ImageButton btnEdit3 = findViewById(R.id.btn_edit_user_3);
        ImageButton btnDelete3 = findViewById(R.id.btn_delete_user_3);

        // Setup Edit Modifiers
        btnEdit1.setOnClickListener(view -> openEditDialog(tvName1, tvEmail1, tvRole1));
        btnEdit2.setOnClickListener(view -> openEditDialog(tvName2, tvEmail2, tvRole2));
        btnEdit3.setOnClickListener(view -> openEditDialog(tvName3, tvEmail3, tvRole3));

        // Setup Delete Triggers
        btnDelete1.setOnClickListener(view -> showDeleteConfirmation(cardUser1, "Rahul Kumar"));
        btnDelete2.setOnClickListener(view -> showDeleteConfirmation(cardUser2, "Alice Williams"));
        btnDelete3.setOnClickListener(view -> showDeleteConfirmation(cardUser3, "Mahesh Shinde"));

        // Setup Search Roster Filter
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRoster(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Dynamic search filtering simulator
    private void filterRoster(String query) {
        String filter = query.toLowerCase();

        // Filter User 1
        if (cardUser1.getVisibility() != View.GONE) {
            boolean matches = tvName1.getText().toString().toLowerCase().contains(filter) ||
                              tvEmail1.getText().toString().toLowerCase().contains(filter);
            cardUser1.setVisibility(matches ? View.VISIBLE : View.INVISIBLE);
        }

        // Filter User 2
        if (cardUser2.getVisibility() != View.GONE) {
            boolean matches = tvName2.getText().toString().toLowerCase().contains(filter) ||
                              tvEmail2.getText().toString().toLowerCase().contains(filter);
            cardUser2.setVisibility(matches ? View.VISIBLE : View.INVISIBLE);
        }

        // Filter User 3
        if (cardUser3.getVisibility() != View.GONE) {
            boolean matches = tvName3.getText().toString().toLowerCase().contains(filter) ||
                              tvEmail3.getText().toString().toLowerCase().contains(filter);
            cardUser3.setVisibility(matches ? View.VISIBLE : View.INVISIBLE);
        }
    }

    // Admin dialog sheets pre-populated for user updates
    private void openEditDialog(TextView tvName, TextView tvEmail, TextView tvRole) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modify User Profile");

        // Simple layout for dialog
        View dialogView = LayoutInflater.from(this).inflate(android.R.layout.activity_list_item, null);
        
        // Let's programmatically assemble the dialog view cleanly
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        TextView labelName = new TextView(this);
        labelName.setText("Full Name");
        labelName.setTextSize(12);
        layout.addView(labelName);

        EditText etName = new EditText(this);
        etName.setText(tvName.getText().toString());
        etName.setTextSize(15);
        layout.addView(etName);

        TextView labelEmail = new TextView(this);
        labelEmail.setText("\nEmail Address");
        labelEmail.setTextSize(12);
        layout.addView(labelEmail);

        EditText etEmail = new EditText(this);
        etEmail.setText(tvEmail.getText().toString());
        etEmail.setTextSize(15);
        layout.addView(etEmail);

        TextView labelRole = new TextView(this);
        labelRole.setText("\nSelect Role");
        labelRole.setTextSize(12);
        layout.addView(labelRole);

        RadioGroup rgRole = new RadioGroup(this);
        rgRole.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton rbStudent = new RadioButton(this);
        rbStudent.setText("STUDENT");
        RadioButton rbMentor = new RadioButton(this);
        rbMentor.setText("MENTOR");
        RadioButton rbAdmin = new RadioButton(this);
        rbAdmin.setText("ADMIN");

        rgRole.addView(rbStudent);
        rgRole.addView(rbMentor);
        rgRole.addView(rbAdmin);

        // Preselect current role
        String currentRole = tvRole.getText().toString();
        if (currentRole.equalsIgnoreCase("STUDENT")) rbStudent.setChecked(true);
        else if (currentRole.equalsIgnoreCase("MENTOR")) rbMentor.setChecked(true);
        else rbAdmin.setChecked(true);

        layout.addView(rgRole);
        builder.setView(layout);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newEmail)) {
                Toast.makeText(AdminUserManagementActivity.this, "Inputs cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Read Selected Radio
            String selectedRole = "STUDENT";
            if (rbMentor.isChecked()) selectedRole = "MENTOR";
            else if (rbAdmin.isChecked()) selectedRole = "ADMIN";

            // Update live fields on current card
            tvName.setText(newName);
            tvEmail.setText(newEmail);
            tvRole.setText(selectedRole);

            // Re-style role background color
            if (selectedRole.equals("STUDENT")) {
                tvRole.setBackgroundResource(R.drawable.bg_social_button);
                tvRole.setBackgroundTintList(getColorStateList(R.color.accent_green_light));
                tvRole.setTextColor(getColor(R.color.accent_green));
            } else if (selectedRole.equals("MENTOR")) {
                tvRole.setBackgroundResource(R.drawable.bg_social_button);
                tvRole.setBackgroundTintList(getColorStateList(R.color.primary_light));
                tvRole.setTextColor(getColor(R.color.primary));
            } else {
                tvRole.setBackgroundResource(R.drawable.bg_social_button);
                tvRole.setBackgroundTintList(getColorStateList(R.color.accent_orange_light));
                tvRole.setTextColor(getColor(R.color.accent_orange));
            }

            Toast.makeText(AdminUserManagementActivity.this, "User profile updated successfully!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Admin Deletions Confirmation Dialogue
    private void showDeleteConfirmation(MaterialCardView card, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User Profile")
                .setMessage("Are you sure you want to permanently delete " + name + " from MenteeConnect? This action is irreversible.")
                .setPositiveButton("Delete Profile", (dialog, which) -> {
                    card.setVisibility(View.GONE);
                    Toast.makeText(AdminUserManagementActivity.this, name + " account deleted successfully.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
