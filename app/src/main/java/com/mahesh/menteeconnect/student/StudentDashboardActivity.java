package com.mahesh.menteeconnect.student;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mahesh.menteeconnect.R;
import com.mahesh.menteeconnect.admin.AdminNetworkClient;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentDashboardActivity extends AppCompatActivity {

    private static final String TAG = "StudentDashboard";

    private TabLayout tabLayout;
    
    // Workspace Elements
    private TextView tvStudentAvatar, tvStudentName, tvStudentIdBatch, tvStudentCourse, tvStudentAttendance, tvStudentCgpa;
    private TextView tvNoMentor, tvMentorAvatar, tvMentorName, tvMentorDesignation, tvMentorDept, tvNoReports;
    private LinearLayout layoutMentorProfile, layoutReportsContainer;
    
    // Tasks Elements
    private LinearLayout layoutTasksContainer;
    private TextView tvNoTasks;
    
    // Vault Elements
    private LinearLayout layoutCertsContainer;
    private TextView tvNoCerts;
    
    private String studentDbId = "";
    private String studentRollId = "";
    private String mentorId = "";
    private String studentName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Tab setup
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Workspace"));
        tabLayout.addTab(tabLayout.newTab().setText("My Tasks"));
        tabLayout.addTab(tabLayout.newTab().setText("Vault"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        findViewById(R.id.scroll_workspace).setVisibility(View.VISIBLE);
                        findViewById(R.id.scroll_tasks).setVisibility(View.GONE);
                        findViewById(R.id.scroll_vault).setVisibility(View.GONE);
                        break;
                    case 1:
                        findViewById(R.id.scroll_workspace).setVisibility(View.GONE);
                        findViewById(R.id.scroll_tasks).setVisibility(View.VISIBLE);
                        findViewById(R.id.scroll_vault).setVisibility(View.GONE);
                        break;
                    case 2:
                        findViewById(R.id.scroll_workspace).setVisibility(View.GONE);
                        findViewById(R.id.scroll_tasks).setVisibility(View.GONE);
                        findViewById(R.id.scroll_vault).setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Initialize UI Elements
        tvStudentAvatar = findViewById(R.id.tv_student_avatar);
        tvStudentName = findViewById(R.id.tv_student_name);
        tvStudentIdBatch = findViewById(R.id.tv_student_id_batch);
        tvStudentCourse = findViewById(R.id.tv_student_course);
        tvStudentAttendance = findViewById(R.id.tv_student_attendance);
        tvStudentCgpa = findViewById(R.id.tv_student_cgpa);
        
        tvNoMentor = findViewById(R.id.tv_no_mentor);
        tvMentorAvatar = findViewById(R.id.tv_mentor_avatar);
        tvMentorName = findViewById(R.id.tv_mentor_name);
        tvMentorDesignation = findViewById(R.id.tv_mentor_designation);
        tvMentorDept = findViewById(R.id.tv_mentor_dept);
        tvNoReports = findViewById(R.id.tv_no_reports);
        
        layoutMentorProfile = findViewById(R.id.layout_mentor_profile);
        layoutReportsContainer = findViewById(R.id.layout_reports_container);
        layoutTasksContainer = findViewById(R.id.layout_student_tasks_container);
        tvNoTasks = findViewById(R.id.tv_no_student_tasks);
        
        layoutCertsContainer = findViewById(R.id.layout_student_certs_container);
        tvNoCerts = findViewById(R.id.tv_no_student_certs);

        // Setup actions
        ImageButton btnRefresh = findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(v -> loadStudentWorkspace());

        ImageButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(view -> {
            com.mahesh.menteeconnect.SessionManager sm = new com.mahesh.menteeconnect.SessionManager(StudentDashboardActivity.this);
            sm.logoutUser();
            com.mahesh.menteeconnect.admin.AdminNetworkClient.setAuthToken(null);
            Intent intent = new Intent(StudentDashboardActivity.this, com.mahesh.menteeconnect.SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Bind to parent card layout click
        findViewById(R.id.cv_upload_cert_trigger).setOnClickListener(v -> showUploadCertificateDialog());

        // Load data
        loadStudentWorkspace();
    }

    private void loadStudentWorkspace() {
        Toast.makeText(this, "Syncing Student Portfolio...", Toast.LENGTH_SHORT).show();
        
        // GET /students/me/profile
        AdminNetworkClient.get("/students/me/profile", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    JSONObject profile = new JSONObject(jsonResponse);
                    studentDbId = profile.optString("id", "");
                    studentRollId = profile.optString("studentId", "");
                    mentorId = profile.optString("mentorId", profile.optString("assignedMentorId", ""));
                    
                    String firstName = profile.optString("firstName", "");
                    String lastName = profile.optString("lastName", "");
                    studentName = firstName + " " + lastName;
                    tvStudentName.setText(studentName);
                    
                    String batch = profile.optString("batch", "2023-2027");
                    String course = profile.optString("course", "Course Details");
                    
                    tvStudentIdBatch.setText("ID: " + studentRollId + " • Batch " + batch);
                    tvStudentCourse.setText(course);
                    
                    if (!firstName.isEmpty()) {
                        tvStudentAvatar.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
                    } else {
                        tvStudentAvatar.setText(String.valueOf(studentName.charAt(0)).toUpperCase());
                    }

                    double attd = profile.optDouble("attendance", 0.0);
                    double cgpa = profile.optDouble("cgpa", 0.0);
                    tvStudentAttendance.setText(String.format("%.1f%%", attd));
                    tvStudentCgpa.setText(String.format("%.2f", cgpa));

                    // Now load secondary systems
                    loadMentorDetails();
                    loadMilestoneReports();
                    loadAssignedTasks();
                    loadUploadedCertificates();

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing student profile", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed loading profile details", e);
                Toast.makeText(StudentDashboardActivity.this, "Connection Error: Offline mode active", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMentorDetails() {
        if (mentorId == null || mentorId.isEmpty() || mentorId.equals("null")) {
            layoutMentorProfile.setVisibility(View.GONE);
            tvNoMentor.setVisibility(View.VISIBLE);
            return;
        }

        // GET /students/my-mentor
        AdminNetworkClient.get("/students/my-mentor", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    JSONObject mentor = new JSONObject(jsonResponse);
                    String name = mentor.optString("firstName", "") + " " + mentor.optString("lastName", "");
                    String designation = mentor.optString("designation", "Academic Advisor");
                    String dept = mentor.optString("department", "Department");

                    tvMentorName.setText(name);
                    tvMentorDesignation.setText(designation);
                    tvMentorDept.setText(dept);

                    if (!name.isEmpty()) {
                        tvMentorAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    }

                    layoutMentorProfile.setVisibility(View.VISIBLE);
                    tvNoMentor.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing advisor info", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed loading advisor details", e);
            }
        });
    }

    private void loadMilestoneReports() {
        // GET /students/progress-reports
        AdminNetworkClient.get("/students/progress-reports", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    layoutReportsContainer.removeAllViews();
                    layoutReportsContainer.addView(tvNoReports);

                    JSONArray arr = new JSONArray(jsonResponse);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject report = arr.getJSONObject(i);
                        tvNoReports.setVisibility(View.GONE);

                        double attendance = report.optDouble("attendance", 0.0);
                        double cgpa = report.optDouble("cgpa", 0.0);
                        String remarks = report.optString("remarks", "Evaluation recorded.");
                        String evaluatedOn = report.optString("evaluatedOn", "Date unknown");

                        View view = LayoutInflater.from(StudentDashboardActivity.this).inflate(android.R.layout.simple_list_item_2, layoutReportsContainer, false);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        text1.setText("CGPA: " + String.format("%.2f", cgpa) + " | Attendance: " + String.format("%.1f%%", attendance));
                        text1.setTextColor(getResources().getColor(R.color.text_primary));
                        text1.setTextSize(13);
                        text1.setTypeface(null, android.graphics.Typeface.BOLD);

                        text2.setText("Evaluated: " + evaluatedOn + "\nRemarks: " + remarks);
                        text2.setTextColor(getResources().getColor(R.color.text_secondary));
                        text2.setTextSize(11);

                        view.setPadding(8, 8, 8, 8);
                        layoutReportsContainer.addView(view);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error rendering milestones", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed loading milestone logs", e);
            }
        });
    }

    private void loadAssignedTasks() {
        if (mentorId.isEmpty() || studentDbId.isEmpty()) {
            return;
        }

        // GET /mentors/{mentorId}/students/{studentId}/tasks
        AdminNetworkClient.get("/mentors/" + mentorId + "/students/" + studentDbId + "/tasks", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    layoutTasksContainer.removeAllViews();
                    layoutTasksContainer.addView(tvNoTasks);

                    JSONArray arr = new JSONArray(jsonResponse);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject task = arr.getJSONObject(i);
                        tvNoTasks.setVisibility(View.GONE);

                        String taskId = task.optString("id", "");
                        String title = task.optString("title", "");
                        String desc = task.optString("description", "");
                        String status = task.optString("status", "ASSIGNED");
                        String dueDate = task.optString("dueDate", "");

                        View view = LayoutInflater.from(StudentDashboardActivity.this).inflate(android.R.layout.simple_list_item_2, layoutTasksContainer, false);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        text1.setText(title + " — " + status);
                        text1.setTextColor(getResources().getColor(R.color.text_primary));
                        text1.setTextSize(13);
                        text1.setTypeface(null, android.graphics.Typeface.BOLD);

                        text2.setText("Due Date: " + dueDate + "\nDetails: " + desc);
                        text2.setTextSize(11);

                        if (status.equalsIgnoreCase("COMPLETED")) {
                            text2.setTextColor(getResources().getColor(R.color.accent_green));
                        } else if (status.equalsIgnoreCase("OVERDUE")) {
                            text2.setTextColor(getResources().getColor(R.color.google_red));
                        } else {
                            text2.setTextColor(getResources().getColor(R.color.accent_purple));
                        }

                        // Toggle status click
                        final String finalTaskId = taskId;
                        final String finalStatus = status;
                        view.setOnClickListener(v -> {
                            String nextStatus = finalStatus.equalsIgnoreCase("COMPLETED") ? "ASSIGNED" : "COMPLETED";
                            new com.google.android.material.dialog.MaterialAlertDialogBuilder(StudentDashboardActivity.this)
                                .setTitle("Update Task Status")
                                .setMessage("Mark this task as " + nextStatus + "?")
                                .setPositiveButton("Yes, Update", (dialog, which) -> {
                                    try {
                                        JSONObject payload = new JSONObject();
                                        payload.put("status", nextStatus);

                                        // PUT /mentors/tasks/{taskId}
                                        AdminNetworkClient.put("/mentors/tasks/" + finalTaskId, payload.toString(), new AdminNetworkClient.ApiCallback() {
                                            @Override
                                            public void onSuccess(String jsonResponse) {
                                                Toast.makeText(StudentDashboardActivity.this, "Task status updated!", Toast.LENGTH_SHORT).show();
                                                loadAssignedTasks();
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                Toast.makeText(StudentDashboardActivity.this, "Failed to update task", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } catch (Exception ex) {
                                        Log.e(TAG, "Error toggling status", ex);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        });

                        view.setPadding(8, 8, 8, 8);
                        layoutTasksContainer.addView(view);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error rendering assigned tasks", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed loading tasks checklist", e);
            }
        });
    }

    private void loadUploadedCertificates() {
        // GET /students/me/certificates
        AdminNetworkClient.get("/students/me/certificates", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    layoutCertsContainer.removeAllViews();
                    layoutCertsContainer.addView(tvNoCerts);

                    JSONArray arr = new JSONArray(jsonResponse);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject cert = arr.getJSONObject(i);
                        tvNoCerts.setVisibility(View.GONE);

                        String certId = cert.optString("id", "");
                        String name = cert.optString("certificateName", "Certificate file");
                        if (name.isEmpty() || name.equals("null")) {
                            name = cert.optString("fileName", "Certificate file");
                        }
                        String desc = cert.optString("description", "");
                        String status = cert.optString("status", "PENDING");
                        String date = cert.optString("uploadDate", "");

                        View view = LayoutInflater.from(StudentDashboardActivity.this).inflate(android.R.layout.simple_list_item_2, layoutCertsContainer, false);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        text1.setText(name + " — " + status);
                        text1.setTextColor(getResources().getColor(status.equalsIgnoreCase("APPROVED") ? R.color.accent_green : R.color.text_primary));
                        text1.setTextSize(13);
                        text1.setTypeface(null, android.graphics.Typeface.BOLD);

                        text2.setText("Upload Date: " + date + "\nDescription: " + desc);
                        text2.setTextColor(getResources().getColor(R.color.text_secondary));
                        text2.setTextSize(11);

                        // Delete capability on long click
                        final String finalCertId = certId;
                        view.setOnLongClickListener(v -> {
                            new com.google.android.material.dialog.MaterialAlertDialogBuilder(StudentDashboardActivity.this)
                                .setTitle("Delete Certificate")
                                .setMessage("Remove this certificate from your academic portfolio?")
                                .setPositiveButton("Remove", (dialog, which) -> {
                                    // DELETE /students/me/certificates/{certificateId}
                                    AdminNetworkClient.delete("/students/me/certificates/" + finalCertId, new AdminNetworkClient.ApiCallback() {
                                        @Override
                                        public void onSuccess(String jsonResponse) {
                                            Toast.makeText(StudentDashboardActivity.this, "Certificate deleted successfully!", Toast.LENGTH_SHORT).show();
                                            loadUploadedCertificates();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(StudentDashboardActivity.this, "Failed to remove certificate", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                            return true;
                        });

                        view.setPadding(8, 8, 8, 8);
                        layoutCertsContainer.addView(view);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error rendering certificates list", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed loading certificates list", e);
            }
        });
    }

    private void showUploadCertificateDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("Upload New Credentials");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 24, 32, 24);

        TextView tvHeading = new TextView(this);
        tvHeading.setText("Add Certificate to Academic Vault");
        tvHeading.setTypeface(null, android.graphics.Typeface.BOLD);
        tvHeading.setPadding(0, 0, 0, 14);
        container.addView(tvHeading);

        EditText etFileName = new EditText(this);
        etFileName.setHint("Certificate File Name (e.g. AWS_Practitioner.pdf)");
        etFileName.setPadding(8, 8, 8, 14);
        container.addView(etFileName);

        EditText etDesc = new EditText(this);
        etDesc.setHint("Provide brief description / remarks...");
        etDesc.setPadding(8, 8, 8, 8);
        container.addView(etDesc);

        builder.setView(container);

        builder.setPositiveButton("Upload Portfolio", (dialog, which) -> {
            String name = etFileName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (name.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Both fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject payload = new JSONObject();
                payload.put("certificateName", name);
                payload.put("filename", name);
                payload.put("certificateUrl", "/uploads/certificates/" + name);
                payload.put("description", desc);

                // POST /students/me/certificates
                AdminNetworkClient.post("/students/me/certificates", payload.toString(), new AdminNetworkClient.ApiCallback() {
                    @Override
                    public void onSuccess(String jsonResponse) {
                        Toast.makeText(StudentDashboardActivity.this, "Certificate uploaded to vault!", Toast.LENGTH_SHORT).show();
                        loadUploadedCertificates();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to upload certificate", e);
                        Toast.makeText(StudentDashboardActivity.this, "Connection Error: Failed to save details", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error constructing certificate payload", e);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
