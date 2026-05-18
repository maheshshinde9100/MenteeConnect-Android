package com.mahesh.menteeconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);

        // Adjust for system status bars and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get Started & Log In Navigation to SignInActivity
        Button btnGetStarted = findViewById(R.id.btn_get_started);
        Button btnLogin = findViewById(R.id.btn_login);

        View.OnClickListener toSignInListener = view -> {
            Intent intent = new Intent(LandingActivity.this, SignInActivity.class);
            startActivity(intent);
        };

        btnGetStarted.setOnClickListener(toSignInListener);
        btnLogin.setOnClickListener(toSignInListener);

        // Developer Section Social Handles
        Button btnGithub = findViewById(R.id.btn_dev_github);
        Button btnLinkedin = findViewById(R.id.btn_dev_linkedin);

        btnGithub.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/maheshshinde9100"));
            startActivity(browserIntent);
        });

        btnLinkedin.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/shindemahesh2112"));
            startActivity(browserIntent);
        });
    }
}
