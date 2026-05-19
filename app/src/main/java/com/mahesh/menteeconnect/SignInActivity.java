package com.mahesh.menteeconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.mahesh.menteeconnect.admin.AdminDashboardActivity;
import com.mahesh.menteeconnect.mentor.MentorDashboardActivity;
import com.mahesh.menteeconnect.student.StudentDashboardActivity;

public class SignInActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        // Adjust for system status bars and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Elements
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        Button btnSignIn = findViewById(R.id.btn_sign_in);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        TextView tvSignUp = findViewById(R.id.tv_sign_up);

        ImageButton btnGoogle = findViewById(R.id.btn_social_google);
        ImageButton btnGithub = findViewById(R.id.btn_social_github);
        ImageButton btnMicrosoft = findViewById(R.id.btn_social_microsoft);
        ImageButton btnFacebook = findViewById(R.id.btn_social_facebook);

        // Social Button Click Handlers
        btnGoogle.setOnClickListener(view -> Toast
                .makeText(SignInActivity.this, "Google Sign-In integration clicked!", Toast.LENGTH_SHORT).show());

        btnGithub.setOnClickListener(view -> Toast
                .makeText(SignInActivity.this, "GitHub Sign-In integration clicked!", Toast.LENGTH_SHORT).show());

        btnMicrosoft.setOnClickListener(view -> Toast
                .makeText(SignInActivity.this, "Microsoft Sign-In integration clicked!", Toast.LENGTH_SHORT).show());

        btnFacebook.setOnClickListener(view -> Toast
                .makeText(SignInActivity.this, "Facebook Sign-In integration clicked!", Toast.LENGTH_SHORT).show());

        // Subsections clicks
        tvForgotPassword.setOnClickListener(view -> Toast
                .makeText(SignInActivity.this, "Password recovery is initialized.", Toast.LENGTH_SHORT).show());

        tvSignUp.setOnClickListener(view -> Toast
                .makeText(SignInActivity.this, "Opening Registration Panel...", Toast.LENGTH_SHORT).show());

        // Sign In Action Click
        btnSignIn.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required");
                return;
            }

            // Send both email and username keys to support backend DTO mapping
            String jsonPayload = "{\"email\":\"" + email + "\",\"username\":\"" + email + "\",\"password\":\""
                    + password + "\"}";

            // Run asynchronous API login handshake
            Toast.makeText(SignInActivity.this, "Connecting to MenteeConnect Security...", Toast.LENGTH_SHORT).show();
            com.mahesh.menteeconnect.admin.AdminNetworkClient.post("/auth/login", jsonPayload,
                    new com.mahesh.menteeconnect.admin.AdminNetworkClient.ApiCallback() {
                        @Override
                        public void onSuccess(String jsonResponse) {
                            try {
                                org.json.JSONObject obj = new org.json.JSONObject(jsonResponse);
                                String token = obj.optString("token", "");
                                String role = "";

                                // Try parsing single role or roles array
                                if (obj.has("role")) {
                                    role = obj.optString("role", "");
                                } else if (obj.has("roles")) {
                                    org.json.JSONArray rolesArr = obj.optJSONArray("roles");
                                    if (rolesArr != null && rolesArr.length() > 0) {
                                        role = rolesArr.optString(0, "");
                                    }
                                }

                                // Also look for username and email in the response if possible
                                String respEmail = obj.optString("email", email);
                                String respName = obj.optString("firstName", "") + " " + obj.optString("lastName", "");
                                if (respName.trim().isEmpty()) {
                                    respName = obj.optString("username",
                                            email.contains("@") ? email.split("@")[0] : email);
                                }

                                if (!token.isEmpty() && !role.isEmpty()) {
                                    com.mahesh.menteeconnect.admin.AdminNetworkClient.setAuthToken(token);
                                    android.util.Log.d("SignInActivity",
                                            "Successfully initialized secure JWT session!");

                                    // Save session persistently
                                    com.mahesh.menteeconnect.SessionManager sessionManager = new com.mahesh.menteeconnect.SessionManager(
                                            SignInActivity.this);
                                    sessionManager.createLoginSession(token, role, respEmail, respName);

                                    navigateToDashboard(respEmail, role);
                                } else {
                                    Toast.makeText(SignInActivity.this, "Login failed: Invalid server response.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                android.util.Log.e("SignInActivity", "Failed to parse JWT payload response", e);
                                Toast.makeText(SignInActivity.this, "Login failed: Error parsing server response.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            String errorMsg = e.getMessage();
                            android.util.Log.e("SignInActivity", "API Sign In failed", e);

                            if (errorMsg != null && (errorMsg.contains("401") || errorMsg.contains("403"))) {
                                Toast.makeText(SignInActivity.this, "Access Denied: Invalid credentials.",
                                        Toast.LENGTH_LONG).show();
                            } else if (errorMsg != null && errorMsg.contains("400")) {
                                Toast.makeText(SignInActivity.this,
                                        "Access Denied: Invalid username or password format.", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                Toast.makeText(SignInActivity.this, "Connection Error: Backend server is unreachable.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });
    }

    private void navigateToDashboard(String email, String role) {
        Toast.makeText(SignInActivity.this, "Welcome to MenteeConnect!", Toast.LENGTH_LONG).show();
        Intent intent;
        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
            intent = new Intent(SignInActivity.this, AdminDashboardActivity.class);
        } else if ("ROLE_MENTOR".equalsIgnoreCase(role)) {
            intent = new Intent(SignInActivity.this, MentorDashboardActivity.class);
        } else {
            intent = new Intent(SignInActivity.this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish(); // prevent returning to sign-in page on back button
    }
}
