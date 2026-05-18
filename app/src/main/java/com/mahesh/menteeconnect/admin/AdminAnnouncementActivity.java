package com.mahesh.menteeconnect.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class AdminAnnouncementActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private RadioGroup rgTarget;
    private LinearLayout layoutAnnouncementsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_announcement);

        // System inset support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize general actions
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(view -> finish());

        // Initialize creation controls
        etTitle = findViewById(R.id.et_announce_title);
        etMessage = findViewById(R.id.et_announce_message);
        rgTarget = findViewById(R.id.rg_target);
        Button btnPublish = findViewById(R.id.btn_publish);
        layoutAnnouncementsList = findViewById(R.id.layout_announcements_list);

        // Publish action click
        btnPublish.setOnClickListener(view -> {
            String title = etTitle.getText().toString().trim();
            String message = etMessage.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message)) {
                Toast.makeText(AdminAnnouncementActivity.this, "Please complete both title and message bodies.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Read target recipient scope
            String scope = "ALL";
            int selectedId = rgTarget.getCheckedRadioButtonId();
            if (selectedId == R.id.rb_mentors) {
                scope = "MENTORS";
            } else if (selectedId == R.id.rb_students) {
                scope = "STUDENTS";
            }

            // Programmatically inflate new announcement log item
            spawnAnnouncementCard(title, message, scope);

            // Clean inputs
            etTitle.setText("");
            etMessage.setText("");
            rgTarget.check(R.id.rb_all); // Reset to All

            Toast.makeText(AdminAnnouncementActivity.this, "STOMP Broadcast alert '" + title + "' published successfully!", Toast.LENGTH_SHORT).show();
        });
    }

    // Dynamic announcement items generator
    private void spawnAnnouncementCard(String title, String message, String scope) {
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

        // Bell Icon
        android.widget.ImageView icon = new android.widget.ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
        icon.setImageResource(R.drawable.ic_certificate);
        icon.setImageTintList(getColorStateList(R.color.google_red));
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
        tvTitle.setText(title);
        tvTitle.setTextColor(getColor(R.color.text_primary));
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        details.addView(tvTitle);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("Scope: " + scope + " | Sent: Just now");
        tvSubtitle.setTextColor(getColor(R.color.text_secondary));
        tvSubtitle.setTextSize(11);
        tvSubtitle.setPadding(0, 4, 0, 0);
        details.addView(tvSubtitle);

        // Body message
        TextView tvMsg = new TextView(this);
        tvMsg.setText(message);
        tvMsg.setTextColor(getColor(R.color.text_secondary));
        tvMsg.setTextSize(13);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgParams.setMargins(0, 20, 0, 0);
        tvMsg.setLayoutParams(msgParams);
        container.addView(tvMsg);

        // Insert at index 0 (top of the lists)
        layoutAnnouncementsList.addView(card, 0);
    }
}
