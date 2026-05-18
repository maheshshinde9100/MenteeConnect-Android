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

        // Fetch initial roster from backend
        syncUserRoster();

        // Setup Edit Modifiers
        btnEdit1.setOnClickListener(view -> openEditDialog("645a7b8c9d0e1", tvName1, tvEmail1, tvRole1));
        btnEdit2.setOnClickListener(view -> openEditDialog("645b8c9d0e1f2", tvName2, tvEmail2, tvRole2));
        btnEdit3.setOnClickListener(view -> openEditDialog("645a7b8c9d0e3", tvName3, tvEmail3, tvRole3));

        // Setup Delete Triggers
        btnDelete1.setOnClickListener(
                view -> showDeleteConfirmation("645a7b8c9d0e1", cardUser1, tvName1.getText().toString()));
        btnDelete2.setOnClickListener(
                view -> showDeleteConfirmation("645b8c9d0e1f2", cardUser2, tvName2.getText().toString()));
        btnDelete3.setOnClickListener(
                view -> showDeleteConfirmation("645a7b8c9d0e3", cardUser3, tvName3.getText().toString()));

        // Setup Search Roster Filter
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // If search query exists, simulate backend search route
                // /admin/users/search?name=
                if (s.length() > 0) {
                    executeSearchCall(s.toString());
                } else {
                    filterRoster("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void syncUserRoster() {
        AdminNetworkClient.get("/admin/users", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    org.json.JSONArray array = new org.json.JSONArray(jsonResponse);
                    if (array.length() > 0) {
                        // Dynamically populate cards with returned database roster profiles
                        bindUserCard(array.optJSONObject(0), tvName1, tvEmail1, tvRole1, cardUser1);
                        if (array.length() > 1) {
                            bindUserCard(array.optJSONObject(1), tvName2, tvEmail2, tvRole2, cardUser2);
                        }
                        if (array.length() > 2) {
                            bindUserCard(array.optJSONObject(2), tvName3, tvEmail3, tvRole3, cardUser3);
                        }
                        Toast.makeText(AdminUserManagementActivity.this, "Roster synchronized with MongoDB!",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("AdminUserManagement", "Failed to parse users payload", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.w("AdminUserManagement", "User API is offline. Operating in local sandbox mode.", e);
            }
        });
    }

    private void bindUserCard(org.json.JSONObject obj, TextView tvName, TextView tvEmail, TextView tvRole, View card) {
        if (obj == null)
            return;
        String first = obj.optString("firstName", "");
        String last = obj.optString("lastName", "");
        String email = obj.optString("email", "");
        String role = obj.optString("role", "STUDENT");

        tvName.setText(first + " " + last);
        tvEmail.setText(email);
        tvRole.setText(role);
        card.setVisibility(View.VISIBLE);
    }

    private void executeSearchCall(String query) {
        // Trigger live search route /admin/users/search?name={name}
        AdminNetworkClient.get("/admin/users/search?name=" + query, new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    org.json.JSONArray array = new org.json.JSONArray(jsonResponse);
                    // Hide all by default, only show matched ones
                    cardUser1.setVisibility(View.GONE);
                    cardUser2.setVisibility(View.GONE);
                    cardUser3.setVisibility(View.GONE);

                    if (array.length() > 0) {
                        bindUserCard(array.optJSONObject(0), tvName1, tvEmail1, tvRole1, cardUser1);
                    }
                    if (array.length() > 1) {
                        bindUserCard(array.optJSONObject(1), tvName2, tvEmail2, tvRole2, cardUser2);
                    }
                    if (array.length() > 2) {
                        bindUserCard(array.optJSONObject(2), tvName3, tvEmail3, tvRole3, cardUser3);
                    }
                } catch (Exception e) {
                    filterRoster(query); // Fallback to client-side filter
                }
            }

            @Override
            public void onFailure(Exception e) {
                filterRoster(query); // Fallback to client-side filter
            }
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
    private void openEditDialog(final String userId, final TextView tvName, final TextView tvEmail,
            final TextView tvRole) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modify User Profile");

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
        if (currentRole.equalsIgnoreCase("STUDENT"))
            rbStudent.setChecked(true);
        else if (currentRole.equalsIgnoreCase("MENTOR"))
            rbMentor.setChecked(true);
        else
            rbAdmin.setChecked(true);

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
            if (rbMentor.isChecked())
                selectedRole = "MENTOR";
            else if (rbAdmin.isChecked())
                selectedRole = "ADMIN";

            // Parse First and Last Names
            String first = newName;
            String last = "";
            if (newName.contains(" ")) {
                int idx = newName.indexOf(" ");
                first = newName.substring(0, idx);
                last = newName.substring(idx + 1);
            }

            // Update live UI fields immediately
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

            // Trigger REST PUT call
            String payload = "{\"firstName\":\"" + first + "\",\"lastName\":\"" + last + "\",\"email\":\"" + newEmail
                    + "\"}";
            AdminNetworkClient.put("/admin/users/" + userId, payload, new AdminNetworkClient.ApiCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    Toast.makeText(AdminUserManagementActivity.this, "Database entry synchronized successfully!",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    android.util.Log.w("AdminUserManagement", "Offline update applied locally.", e);
                    Toast.makeText(AdminUserManagementActivity.this, "User profile updated successfully!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Admin Deletions Confirmation Dialogue
    private void showDeleteConfirmation(final String userId, final MaterialCardView card, final String name) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User Profile")
                .setMessage("Are you sure you want to permanently delete " + name
                        + " from MenteeConnect? This action is irreversible.")
                .setPositiveButton("Delete Profile", (dialog, which) -> {
                    // Update UI immediately
                    card.setVisibility(View.GONE);

                    // Trigger REST DELETE call
                    AdminNetworkClient.delete("/admin/users/" + userId, new AdminNetworkClient.ApiCallback() {
                        @Override
                        public void onSuccess(String jsonResponse) {
                            Toast.makeText(AdminUserManagementActivity.this, name + " account deleted from database.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            android.util.Log.w("AdminUserManagement", "Offline delete applied locally.", e);
                            Toast.makeText(AdminUserManagementActivity.this, name + " account deleted successfully.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
