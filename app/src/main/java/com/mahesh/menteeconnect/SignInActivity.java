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

            // Mock Success Sign In
            Toast.makeText(SignInActivity.this, "Welcome to MenteeConnect!", Toast.LENGTH_LONG).show();
            
            // Navigate to appropriate dashboard based on user role mapping
            Intent intent;
            if (email.toLowerCase().contains("admin")) {
                intent = new Intent(SignInActivity.this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(SignInActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish(); // prevent returning to sign-in page on back button
        });

        // Social Button Click Handlers
        btnGoogle.setOnClickListener(view -> 
            Toast.makeText(SignInActivity.this, "Google Sign-In integration clicked!", Toast.LENGTH_SHORT).show()
        );

        btnGithub.setOnClickListener(view -> 
            Toast.makeText(SignInActivity.this, "GitHub Sign-In integration clicked!", Toast.LENGTH_SHORT).show()
        );

        btnMicrosoft.setOnClickListener(view -> 
            Toast.makeText(SignInActivity.this, "Microsoft Sign-In integration clicked!", Toast.LENGTH_SHORT).show()
        );

        btnFacebook.setOnClickListener(view -> 
            Toast.makeText(SignInActivity.this, "Facebook Sign-In integration clicked!", Toast.LENGTH_SHORT).show()
        );

        // Subsections clicks
        tvForgotPassword.setOnClickListener(view -> 
            Toast.makeText(SignInActivity.this, "Password recovery is initialized.", Toast.LENGTH_SHORT).show()
        );

        tvSignUp.setOnClickListener(view -> 
            Toast.makeText(SignInActivity.this, "Opening Registration Panel...", Toast.LENGTH_SHORT).show()
        );
    }
}
