package com.mahesh.menteeconnect.mentor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.mahesh.menteeconnect.R;
import com.mahesh.menteeconnect.admin.AdminNetworkClient;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MentorAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "MentorAttendance";

    private String mentorId = "";
    private List<JSONObject> studentList = new ArrayList<>();
    private List<String> studentNames = new ArrayList<>();

    private Spinner spinnerStudents;
    private EditText etDate, etRemarks;
    private RadioButton rbPresent, rbAbsent;
    private LinearLayout layoutLogsContainer;
    private TextView tvNoLogs;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_attendance);

        mentorId = getIntent().getStringExtra("mentorId");

        // Initialize UI Elements
        spinnerStudents = findViewById(R.id.spinner_students);
        etDate = findViewById(R.id.et_attendance_date);
        etRemarks = findViewById(R.id.et_attendance_remarks);
        rbPresent = findViewById(R.id.rb_present);
        rbAbsent = findViewById(R.id.rb_absent);
        layoutLogsContainer = findViewById(R.id.layout_attendance_logs_container);
        tvNoLogs = findViewById(R.id.tv_no_attendance_logs);
        pbLoading = findViewById(R.id.pb_attendance_loading);

        // Pre-fill today's date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(new Date()));

        // Back Button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Submit Button
        MaterialButton btnSubmit = findViewById(R.id.btn_submit_attendance);
        btnSubmit.setOnClickListener(v -> submitAttendance());

        // Initial fetch
        loadMentees();
        loadAttendanceHistory();
    }

    private void loadMentees() {
        if (mentorId == null || mentorId.isEmpty()) {
            return;
        }

        AdminNetworkClient.get("/mentors/" + mentorId + "/students", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    studentList.clear();
                    studentNames.clear();

                    JSONArray arr = new JSONArray(jsonResponse);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject student = arr.getJSONObject(i);
                        studentList.add(student);
                        
                        String name = student.optString("firstName", "") + " " + student.optString("lastName", "");
                        String rollId = student.optString("studentId", "");
                        studentNames.add(name + " (" + rollId + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MentorAttendanceActivity.this,
                            android.R.layout.simple_spinner_item, studentNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerStudents.setAdapter(adapter);

                } catch (Exception e) {
                    Log.e(TAG, "Error loading spinner mentees", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed loading mentees spinner data", e);
            }
        });
    }

    private void loadAttendanceHistory() {
        if (mentorId == null || mentorId.isEmpty()) {
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        AdminNetworkClient.get("/mentors/" + mentorId + "/attendance", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                pbLoading.setVisibility(View.GONE);
                try {
                    JSONArray arr = new JSONArray(jsonResponse);
                    populateLogsList(arr);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing attendance logs history", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "Failed to load attendance logs", e);
            }
        });
    }

    private void populateLogsList(JSONArray logs) {
        layoutLogsContainer.removeAllViews();
        layoutLogsContainer.addView(tvNoLogs);

        try {
            for (int i = 0; i < logs.length(); i++) {
                JSONObject logEntry = logs.getJSONObject(i);
                tvNoLogs.setVisibility(View.GONE);

                String logId = logEntry.optString("id", "");
                String date = logEntry.optString("date", "");
                String status = logEntry.optString("status", "PRESENT");
                String remarks = logEntry.optString("remarks", "");
                String studentName = logEntry.optString("studentName", "Student");

                if (studentName.equals("Student") || studentName.isEmpty()) {
                    // Match studentName from studentList if empty
                    String logStudentId = logEntry.optString("studentId", "");
                    for (JSONObject stud : studentList) {
                        if (stud.optString("id", "").equals(logStudentId)) {
                            studentName = stud.optString("firstName", "") + " " + stud.optString("lastName", "");
                            break;
                        }
                    }
                }

                View view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, layoutLogsContainer, false);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(studentName + " — " + status);
                text1.setTextColor(getResources().getColor(status.equalsIgnoreCase("PRESENT") ? R.color.accent_green : R.color.google_red));
                text1.setTextSize(13);
                text1.setTypeface(null, android.graphics.Typeface.BOLD);
                
                text2.setText("Date: " + date + " | Remarks: " + (remarks.isEmpty() ? "None" : remarks));
                text2.setTextColor(getResources().getColor(R.color.text_secondary));
                text2.setTextSize(11);

                // Add delete capability on long click
                final String finalLogId = logId;
                view.setOnLongClickListener(v -> {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(MentorAttendanceActivity.this)
                        .setTitle("Delete Attendance Log")
                        .setMessage("Are you sure you want to remove this attendance log entry?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            AdminNetworkClient.delete("/mentors/attendance/" + finalLogId, new AdminNetworkClient.ApiCallback() {
                                @Override
                                public void onSuccess(String jsonResponse) {
                                    Toast.makeText(MentorAttendanceActivity.this, "Log deleted successfully!", Toast.LENGTH_SHORT).show();
                                    loadAttendanceHistory();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(MentorAttendanceActivity.this, "Failed to delete log", Toast.LENGTH_SHORT).show();
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
            Log.e(TAG, "Error rendering attendance logs in dynamic UI", e);
        }
    }

    private void submitAttendance() {
        if (studentList.isEmpty()) {
            Toast.makeText(this, "No mentees assigned to record attendance for!", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spinnerStudents.getSelectedItemPosition();
        if (pos < 0 || pos >= studentList.size()) {
            Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject student = studentList.get(pos);
        String studentDbId = student.optString("id", "");

        String dateStr = etDate.getText().toString().trim();
        String remarksStr = etRemarks.getText().toString().trim();

        if (dateStr.isEmpty() || remarksStr.isEmpty()) {
            Toast.makeText(this, "Date and Remarks details are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        String status = rbPresent.isChecked() ? "PRESENT" : "ABSENT";

        try {
            JSONObject payload = new JSONObject();
            payload.put("studentId", studentDbId);
            payload.put("date", dateStr);
            payload.put("status", status);
            payload.put("remarks", remarksStr);

            // POST /mentors/{mentorId}/attendance
            AdminNetworkClient.post("/mentors/" + mentorId + "/attendance", payload.toString(), new AdminNetworkClient.ApiCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    Toast.makeText(MentorAttendanceActivity.this, "Attendance logged successfully!", Toast.LENGTH_SHORT).show();
                    etRemarks.setText(""); // clear remarks input
                    loadAttendanceHistory(); // reload history
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed logging attendance", e);
                    Toast.makeText(MentorAttendanceActivity.this, "Connection Error: Failed to record attendance", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error constructing attendance payload", e);
        }
    }
}
