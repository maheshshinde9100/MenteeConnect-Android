package com.mahesh.menteeconnect.mentor;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.mahesh.menteeconnect.R;
import com.mahesh.menteeconnect.admin.AdminNetworkClient;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MenteeDetailActivity extends AppCompatActivity {

    private static final String TAG = "MenteeDetail";

    private String mentorId = "";
    private String studentId = ""; // DB MongoDB Object ID
    private String studentRollId = ""; // Academic ID like STU1001

    private TextView tvAvatar, tvName, tvId, tvCourseBatch, tvEmailPhone, tvCertsCount, tvNoCerts, tvNoTasks;
    private EditText etAttendance, etCgpa, etNotes;
    private LinearLayout layoutCertsContainer, layoutTasksContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentee_detail);

        // Retrieve parameters from Intent
        mentorId = getIntent().getStringExtra("mentorId");
        studentId = getIntent().getStringExtra("studentId");
        studentRollId = getIntent().getStringExtra("studentRollId");
        String studentName = getIntent().getStringExtra("studentName");
        String course = getIntent().getStringExtra("studentCourse");
        String batch = getIntent().getStringExtra("studentBatch");
        String email = getIntent().getStringExtra("studentEmail");
        String phone = getIntent().getStringExtra("studentPhone");
        double attd = getIntent().getDoubleExtra("studentAttendance", 0.0);
        double cgpa = getIntent().getDoubleExtra("studentCgpa", 0.0);

        // Initialize UI Elements
        tvAvatar = findViewById(R.id.tv_detail_avatar);
        tvName = findViewById(R.id.tv_detail_name);
        tvId = findViewById(R.id.tv_detail_id);
        tvCourseBatch = findViewById(R.id.tv_detail_course_batch);
        tvEmailPhone = findViewById(R.id.tv_detail_email_phone);
        tvCertsCount = findViewById(R.id.tv_detail_certs_count);
        tvNoCerts = findViewById(R.id.tv_no_certificates);
        tvNoTasks = findViewById(R.id.tv_no_tasks);
        
        etAttendance = findViewById(R.id.et_detail_attendance);
        etCgpa = findViewById(R.id.et_detail_cgpa);
        etNotes = findViewById(R.id.et_detail_notes);
        
        layoutCertsContainer = findViewById(R.id.layout_certificates_container);
        layoutTasksContainer = findViewById(R.id.layout_tasks_container);

        // Fill initial static details
        tvName.setText(studentName);
        tvId.setText("Student ID: " + studentRollId);
        tvCourseBatch.setText(course + " • Batch " + batch);
        tvEmailPhone.setText((email.isEmpty() ? "No email" : email) + " | " + (phone.isEmpty() ? "No phone" : phone));
        
        if (studentName != null && !studentName.isEmpty()) {
            tvAvatar.setText(String.valueOf(studentName.charAt(0)).toUpperCase());
        }

        etAttendance.setText(String.valueOf(attd));
        etCgpa.setText(String.valueOf(cgpa));

        // Listeners
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        MaterialButton btnSave = findViewById(R.id.btn_save_updates);
        btnSave.setOnClickListener(v -> saveProgressUpdates());

        MaterialButton btnAssignTask = findViewById(R.id.btn_assign_task);
        btnAssignTask.setOnClickListener(v -> showAssignTaskDialog());

        // Fetch Full Profile including Tasks & Certificates
        fetchDetailedProfile();
    }

    private void fetchDetailedProfile() {
        if (mentorId == null || mentorId.isEmpty() || studentId == null || studentId.isEmpty()) {
            return;
        }

        AdminNetworkClient.get("/mentors/" + mentorId + "/students/" + studentId, new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    JSONObject details = new JSONObject(jsonResponse);
                    
                    // Display existing notes
                    String notes = details.optString("mentorNotes", "");
                    if (notes.isEmpty() || notes.equals("null")) {
                        notes = details.optString("notes", "");
                    }
                    etNotes.setText(notes);

                    // Fetch certificates
                    loadStudentCertificates();

                    // Fetch tasks
                    loadStudentTasks();
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching detailed profile", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load detailed profile", e);
                // Try fallback to load certificates/tasks list independently
                loadStudentCertificates();
                loadStudentTasks();
            }
        });
    }

    private void loadStudentCertificates() {
        // GET /students/{id}/certificates (we can query the student's certificates directly or through fallback API)
        // Since we are in the mentor view, we can render the certificates if we have them
        // Let's query them or let's use the certificates list from the detailed profile or generic student portfolio
        AdminNetworkClient.get("/students/me/certificates", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    JSONArray arr = new JSONArray(jsonResponse);
                    populateCertificates(arr);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing certificates", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch student certificates directly", e);
            }
        });
    }

    private void populateCertificates(JSONArray certificates) {
        layoutCertsContainer.removeAllViews();
        layoutCertsContainer.addView(tvNoCerts);
        
        int certCount = 0;
        try {
            for (int i = 0; i < certificates.length(); i++) {
                JSONObject cert = certificates.getJSONObject(i);
                
                // Make sure it belongs to this student
                String certStudentId = cert.optString("studentId", "");
                if (!certStudentId.equals(studentId)) {
                    continue;
                }

                certCount++;
                tvNoCerts.setVisibility(View.GONE);

                String name = cert.optString("certificateName", "Certificate file");
                String desc = cert.optString("description", "");
                String status = cert.optString("status", "PENDING");

                View view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, layoutCertsContainer, false);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(name);
                text1.setTextColor(getResources().getColor(R.color.text_primary));
                text1.setTextSize(13);
                
                text2.setText(desc + " | Status: " + status);
                text2.setTextColor(getResources().getColor(status.equalsIgnoreCase("APPROVED") ? R.color.accent_green : R.color.text_secondary));
                text2.setTextSize(11);

                view.setPadding(8, 8, 8, 8);
                layoutCertsContainer.addView(view);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rendering certificates", e);
        }

        tvCertsCount.setText(certCount + " files");
    }

    private void loadStudentTasks() {
        AdminNetworkClient.get("/mentors/" + mentorId + "/students/" + studentId + "/tasks", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    JSONArray arr = new JSONArray(jsonResponse);
                    populateTasks(arr);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing tasks response", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load student tasks", e);
            }
        });
    }

    private void populateTasks(JSONArray tasks) {
        layoutTasksContainer.removeAllViews();
        layoutTasksContainer.addView(tvNoTasks);

        try {
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject task = tasks.getJSONObject(i);
                tvNoTasks.setVisibility(View.GONE);

                String title = task.optString("title", "");
                String status = task.optString("status", "ASSIGNED");
                String dueDate = task.optString("dueDate", "");

                View view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, layoutTasksContainer, false);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(title);
                text1.setTextColor(getResources().getColor(R.color.text_primary));
                text1.setTextSize(13);
                
                text2.setText("Due: " + dueDate + " • Status: " + status);
                text2.setTextSize(11);
                
                if (status.equalsIgnoreCase("COMPLETED")) {
                    text2.setTextColor(getResources().getColor(R.color.accent_green));
                } else if (status.equalsIgnoreCase("OVERDUE")) {
                    text2.setTextColor(getResources().getColor(R.color.google_red));
                } else {
                    text2.setTextColor(getResources().getColor(R.color.accent_purple));
                }

                view.setPadding(8, 8, 8, 8);
                layoutTasksContainer.addView(view);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rendering tasks list", e);
        }
    }

    private void saveProgressUpdates() {
        String attdStr = etAttendance.getText().toString().trim();
        String cgpaStr = etCgpa.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (attdStr.isEmpty() || cgpaStr.isEmpty()) {
            Toast.makeText(this, "Please enter both CGPA and Attendance values", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double attendance = Double.parseDouble(attdStr);
            double cgpa = Double.parseDouble(cgpaStr);

            JSONObject payload = new JSONObject();
            payload.put("notes", notes);
            payload.put("mentorNotes", notes);
            payload.put("attendance", attendance);
            payload.put("cgpa", cgpa);

            // PUT /mentors/{mentorId}/students/{studentId}
            AdminNetworkClient.put("/mentors/" + mentorId + "/students/" + studentId, payload.toString(), new AdminNetworkClient.ApiCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    Toast.makeText(MenteeDetailActivity.this, "Student evaluation saved successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to save evaluation", e);
                    Toast.makeText(MenteeDetailActivity.this, "Server Error: Failed to save updates", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Please enter valid numeric formats", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAssignTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Assign New Academic Task");

        View view = LayoutInflater.from(this).inflate(R.layout.activity_mentor_task, null);
        // Clean layout to present as dialog form
        view.findViewById(R.id.btn_back).setVisibility(View.GONE);
        view.findViewById(R.id.spinner_students).setVisibility(View.GONE);
        view.findViewById(R.id.layout_tasks_logs_container).getParent().getParent().setVisibility(View.GONE);

        EditText etTitle = view.findViewById(R.id.et_task_title);
        EditText etDesc = view.findViewById(R.id.et_task_description);
        EditText etDueDate = view.findViewById(R.id.et_task_due_date);
        RadioGroup rgPriority = view.findViewById(R.id.rg_priority);

        // Pre-fill due date with 7 days from now
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        etDueDate.setText(sdf.format(cal.getTime()));

        builder.setView(view);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Deploy Task", null); // Set listener later to override default dismiss behavior

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button click to validate inputs
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String due = etDueDate.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty() || due.isEmpty()) {
                Toast.makeText(this, "All details are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            String priority = "MEDIUM";
            int checkedId = rgPriority.getCheckedRadioButtonId();
            if (checkedId == R.id.rb_low) {
                priority = "LOW";
            } else if (checkedId == R.id.rb_high) {
                priority = "HIGH";
            }

            try {
                JSONObject payload = new JSONObject();
                payload.put("studentId", studentId);
                payload.put("title", title);
                payload.put("description", desc);
                payload.put("dueDate", due);
                payload.put("priority", priority);

                AdminNetworkClient.post("/mentors/" + mentorId + "/tasks", payload.toString(), new AdminNetworkClient.ApiCallback() {
                    @Override
                    public void onSuccess(String jsonResponse) {
                        Toast.makeText(MenteeDetailActivity.this, "Task assigned successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadStudentTasks(); // Reload tasks view
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to assign task", e);
                        Toast.makeText(MenteeDetailActivity.this, "Failed to deploy task", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error constructing task payload", e);
            }
        });
    }
}
