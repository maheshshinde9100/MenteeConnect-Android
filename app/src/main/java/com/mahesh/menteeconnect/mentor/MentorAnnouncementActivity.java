package com.mahesh.menteeconnect.mentor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.mahesh.menteeconnect.R;
import com.mahesh.menteeconnect.admin.AdminNetworkClient;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MentorAnnouncementActivity extends AppCompatActivity {

    private static final String TAG = "MentorAnnouncement";

    private String mentorId = "";
    private List<String> studentIds = new ArrayList<>();

    private EditText etTitle, etContent, etLink;
    private LinearLayout layoutLogsContainer;
    private TextView tvNoLogs;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_announcement);

        mentorId = getIntent().getStringExtra("mentorId");

        // Initialize UI Elements
        etTitle = findViewById(R.id.et_announcement_title);
        etContent = findViewById(R.id.et_announcement_content);
        etLink = findViewById(R.id.et_announcement_link);
        layoutLogsContainer = findViewById(R.id.layout_announcements_logs_container);
        tvNoLogs = findViewById(R.id.tv_no_announcements);
        pbLoading = findViewById(R.id.pb_announcements_loading);

        // Back Button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Publish Button
        MaterialButton btnPublish = findViewById(R.id.btn_publish_announcement);
        btnPublish.setOnClickListener(v -> publishAnnouncement());

        // Initial fetch
        loadMentees();
        loadAnnouncementsHistory();
    }

    private void loadMentees() {
        if (mentorId == null || mentorId.isEmpty()) {
            return;
        }

        AdminNetworkClient.get("/mentors/" + mentorId + "/students", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    studentIds.clear();
                    JSONArray arr = new JSONArray(jsonResponse);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject student = arr.getJSONObject(i);
                        studentIds.add(student.optString("id", ""));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing mentees list for announcement target IDs", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load mentees", e);
            }
        });
    }

    private void loadAnnouncementsHistory() {
        if (mentorId == null || mentorId.isEmpty()) {
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        AdminNetworkClient.get("/mentors/" + mentorId + "/updates", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                pbLoading.setVisibility(View.GONE);
                try {
                    JSONArray arr = new JSONArray(jsonResponse);
                    populateAnnouncementsList(arr);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing updates list response", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "Failed loading updates list", e);
            }
        });
    }

    private void populateAnnouncementsList(JSONArray logs) {
        layoutLogsContainer.removeAllViews();
        layoutLogsContainer.addView(tvNoLogs);

        try {
            for (int i = 0; i < logs.length(); i++) {
                JSONObject update = logs.getJSONObject(i);
                tvNoLogs.setVisibility(View.GONE);

                String updateId = update.optString("id", "");
                String title = update.optString("title", "");
                String content = update.optString("content", "");
                boolean important = update.optBoolean("important", false);

                JSONArray linksArr = update.optJSONArray("attachmentUrls");
                String attachmentLink = "";
                if (linksArr != null && linksArr.length() > 0) {
                    attachmentLink = linksArr.optString(0, "");
                }

                View view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, layoutLogsContainer, false);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(title + (important ? " [URGENT]" : ""));
                text1.setTextColor(getResources().getColor(important ? R.color.google_red : R.color.text_primary));
                text1.setTextSize(13);
                text1.setTypeface(null, android.graphics.Typeface.BOLD);
                
                String body = content;
                if (!attachmentLink.isEmpty()) {
                    body += "\nLink: " + attachmentLink;
                }
                text2.setText(body);
                text2.setTextColor(getResources().getColor(R.color.text_secondary));
                text2.setTextSize(11);

                final String finalLink = attachmentLink;
                if (!finalLink.isEmpty()) {
                    view.setOnClickListener(v -> {
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalLink));
                            startActivity(browserIntent);
                        } catch (Exception e) {
                            Toast.makeText(MentorAnnouncementActivity.this, "Invalid announcement link", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Add delete capability on long click
                final String finalUpdateId = updateId;
                view.setOnLongClickListener(v -> {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(MentorAnnouncementActivity.this)
                        .setTitle("Delete Announcement")
                        .setMessage("Are you sure you want to remove this notice board entry?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            AdminNetworkClient.delete("/mentors/updates/" + finalUpdateId, new AdminNetworkClient.ApiCallback() {
                                @Override
                                public void onSuccess(String jsonResponse) {
                                    Toast.makeText(MentorAnnouncementActivity.this, "Announcement deleted successfully!", Toast.LENGTH_SHORT).show();
                                    loadAnnouncementsHistory();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(MentorAnnouncementActivity.this, "Failed to delete notice", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                    return true;
                });

                view.setPadding(8, 8, 8, 8);
                layoutLogsContainer.addView(view);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rendering announcement log entry in UI", e);
        }
    }

    private void publishAnnouncement() {
        String titleStr = etTitle.getText().toString().trim();
        String contentStr = etContent.getText().toString().trim();
        String linkStr = etLink.getText().toString().trim();

        if (titleStr.isEmpty() || contentStr.isEmpty()) {
            Toast.makeText(this, "Title and message details are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("title", titleStr);
            payload.put("content", contentStr);
            payload.put("important", true); // Set as standard notice board alert

            // Target all assigned students
            JSONArray targetIds = new JSONArray();
            for (String studId : studentIds) {
                targetIds.put(studId);
            }
            payload.put("studentIds", targetIds);

            if (!linkStr.isEmpty()) {
                JSONArray attachmentUrls = new JSONArray();
                attachmentUrls.put(linkStr);
                payload.put("attachmentUrls", attachmentUrls);
            }

            // POST /mentors/{mentorId}/updates
            AdminNetworkClient.post("/mentors/" + mentorId + "/updates", payload.toString(), new AdminNetworkClient.ApiCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    Toast.makeText(MentorAnnouncementActivity.this, "Notice broadcasted successfully!", Toast.LENGTH_SHORT).show();
                    etTitle.setText("");
                    etContent.setText("");
                    etLink.setText("");
                    loadAnnouncementsHistory(); // reload history notice board list
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed posting update announcement", e);
                    Toast.makeText(MentorAnnouncementActivity.this, "Connection Error: Failed to publish update", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error constructing announcement payload", e);
        }
    }
}
