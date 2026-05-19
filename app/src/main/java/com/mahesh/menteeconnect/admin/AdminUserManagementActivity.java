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
import org.json.JSONArray;
import org.json.JSONObject;

public class AdminUserManagementActivity extends AppCompatActivity {

    private LinearLayout layoutUserList;
    private JSONArray usersArray = new JSONArray();

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

        // Bind listing layout
        layoutUserList = findViewById(R.id.layout_user_list);

        // Fetch initial roster from backend
        syncUserRoster();

        // Setup Search Roster Filter
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    executeSearchCall(s.toString());
                } else {
                    renderUserList(usersArray);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void syncUserRoster() {
        AdminNetworkClient.get("/admin/users", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    usersArray = new JSONArray(jsonResponse);
                    renderUserList(usersArray);
                    Toast.makeText(AdminUserManagementActivity.this, "Roster synchronized with MongoDB!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    android.util.Log.e("AdminUserManagement", "Failed to parse users payload", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.w("AdminUserManagement", "User API offline or loading error", e);
                Toast.makeText(AdminUserManagementActivity.this, "Sync failed: showing cached roster", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderUserList(JSONArray array) {
        layoutUserList.removeAllViews();
        if (array == null || array.length() == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No matching users found.");
            tvEmpty.setPadding(32, 32, 32, 32);
            tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary));
            tvEmpty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layoutUserList.addView(tvEmpty);
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                View view = LayoutInflater.from(this).inflate(R.layout.item_user_card, layoutUserList, false);

                MaterialCardView cardUser = view.findViewById(R.id.card_user);
                TextView tvUserAvatar = view.findViewById(R.id.tv_user_avatar);
                TextView tvUserName = view.findViewById(R.id.tv_user_name);
                TextView tvUserEmail = view.findViewById(R.id.tv_user_email);
                TextView tvUserRole = view.findViewById(R.id.tv_user_role);
                ImageButton btnEditUser = view.findViewById(R.id.btn_edit_user);
                ImageButton btnDeleteUser = view.findViewById(R.id.btn_delete_user);

                final String userId = obj.optString("id", "");
                final String first = obj.optString("firstName", "");
                final String last = obj.optString("lastName", "");
                final String name = first + " " + last;
                final String email = obj.optString("email", "");
                final String role = obj.optString("role", "STUDENT");

                tvUserName.setText(name.trim().isEmpty() ? "Anonymous User" : name);
                tvUserEmail.setText(email);
                tvUserRole.setText(role);

                // Set Avatar Text
                if (!first.isEmpty()) {
                    tvUserAvatar.setText(String.valueOf(first.charAt(0)).toUpperCase());
                } else if (!name.isEmpty() && !name.trim().isEmpty()) {
                    tvUserAvatar.setText(String.valueOf(name.trim().charAt(0)).toUpperCase());
                } else {
                    tvUserAvatar.setText("?");
                }

                // Set Badge styles
                if (role.equalsIgnoreCase("STUDENT")) {
                    tvUserRole.setBackgroundTintList(getColorStateList(R.color.accent_green_light));
                    tvUserRole.setTextColor(getColor(R.color.accent_green));
                } else if (role.equalsIgnoreCase("MENTOR")) {
                    tvUserRole.setBackgroundTintList(getColorStateList(R.color.primary_light));
                    tvUserRole.setTextColor(getColor(R.color.primary));
                } else {
                    tvUserRole.setBackgroundTintList(getColorStateList(R.color.accent_orange_light));
                    tvUserRole.setTextColor(getColor(R.color.accent_orange));
                }

                // Bind listener actions
                btnEditUser.setOnClickListener(v -> openEditDialog(userId, name, email, role));
                btnDeleteUser.setOnClickListener(v -> showDeleteConfirmation(userId, cardUser, name));

                layoutUserList.addView(view);

            } catch (Exception e) {
                android.util.Log.e("AdminUserManagement", "Error rendering user item", e);
            }
        }
    }

    private void executeSearchCall(String query) {
        // Trigger live search route /admin/users/search?name={name}
        AdminNetworkClient.get("/admin/users/search?name=" + query, new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    JSONArray array = new JSONArray(jsonResponse);
                    renderUserList(array);
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

    // Dynamic search filtering local fallback
    private void filterRoster(String query) {
        String filter = query.toLowerCase();
        JSONArray filtered = new JSONArray();
        for (int i = 0; i < usersArray.length(); i++) {
            try {
                JSONObject obj = usersArray.getJSONObject(i);
                String first = obj.optString("firstName", "").toLowerCase();
                String last = obj.optString("lastName", "").toLowerCase();
                String email = obj.optString("email", "").toLowerCase();
                if (first.contains(filter) || last.contains(filter) || email.contains(filter)) {
                    filtered.put(obj);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        renderUserList(filtered);
    }

    private void openEditDialog(final String userId, final String name, final String email, final String role) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modify User Profile");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        TextView labelName = new TextView(this);
        labelName.setText("Full Name");
        labelName.setTextSize(12);
        layout.addView(labelName);

        EditText etName = new EditText(this);
        etName.setText(name);
        etName.setTextSize(15);
        layout.addView(etName);

        TextView labelEmail = new TextView(this);
        labelEmail.setText("\nEmail Address");
        labelEmail.setTextSize(12);
        layout.addView(labelEmail);

        EditText etEmail = new EditText(this);
        etEmail.setText(email);
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

        if (role.equalsIgnoreCase("STUDENT"))
            rbStudent.setChecked(true);
        else if (role.equalsIgnoreCase("MENTOR"))
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

            String selectedRole = "STUDENT";
            if (rbMentor.isChecked())
                selectedRole = "MENTOR";
            else if (rbAdmin.isChecked())
                selectedRole = "ADMIN";

            String first = newName;
            String last = "";
            if (newName.contains(" ")) {
                int idx = newName.indexOf(" ");
                first = newName.substring(0, idx);
                last = newName.substring(idx + 1);
            }

            // Trigger REST PUT call
            try {
                JSONObject payload = new JSONObject();
                payload.put("firstName", first);
                payload.put("lastName", last);
                payload.put("email", newEmail);
                payload.put("role", selectedRole);

                AdminNetworkClient.put("/admin/users/" + userId, payload.toString(), new AdminNetworkClient.ApiCallback() {
                    @Override
                    public void onSuccess(String jsonResponse) {
                        Toast.makeText(AdminUserManagementActivity.this, "User profile updated successfully!", Toast.LENGTH_SHORT).show();
                        syncUserRoster();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        android.util.Log.e("AdminUserManagement", "Failed to update profile", e);
                        Toast.makeText(AdminUserManagementActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception ex) {
                android.util.Log.e("AdminUserManagement", "Error building update request", ex);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmation(final String userId, final MaterialCardView card, final String name) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User Profile")
                .setMessage("Are you sure you want to permanently delete " + name + " from MenteeConnect? This action is irreversible.")
                .setPositiveButton("Delete Profile", (dialog, which) -> {
                    AdminNetworkClient.delete("/admin/users/" + userId, new AdminNetworkClient.ApiCallback() {
                        @Override
                        public void onSuccess(String jsonResponse) {
                            Toast.makeText(AdminUserManagementActivity.this, name + " account deleted from database.", Toast.LENGTH_SHORT).show();
                            syncUserRoster();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            android.util.Log.e("AdminUserManagement", "Failed to delete account", e);
                            Toast.makeText(AdminUserManagementActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
