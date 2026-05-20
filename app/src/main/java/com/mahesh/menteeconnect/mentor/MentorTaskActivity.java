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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mahesh.menteeconnect.R;
import com.mahesh.menteeconnect.admin.AdminNetworkClient;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MentorTaskActivity extends AppCompatActivity {

    private static final String TAG = "MentorTask";

    private String mentorId = "";
    private List<JSONObject> studentList = new ArrayList<>();
    private List<String> studentNames = new ArrayList<>();

    private Spinner spinnerStudents;
    private EditText etTitle, etDescription, etDueDate;
    private RadioGroup rgPriority;
    private LinearLayout layoutLogsContainer;
    private TextView tvNoLogs;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_task);

        mentorId = getIntent().getStringExtra("mentorId");

        // Initialize UI Elements
        spinnerStudents = findViewById(R.id.spinner_students);
        etTitle = findViewById(R.id.et_task_title);
        etDescription = findViewById(R.id.et_task_description);
        etDueDate = findViewById(R.id.et_task_due_date);
        rgPriority = findViewById(R.id.rg_priority);
        layoutLogsContainer = findViewById(R.id.layout_tasks_logs_container);
        tvNoLogs = findViewById(R.id.tv_no_tasks_logs);
        pbLoading = findViewById(R.id.pb_tasks_loading);

        // Pre-fill due date with 7 days from now
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        etDueDate.setText(sdf.format(cal.getTime()));

        // Back Button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Submit Button
        MaterialButton btnSubmit = findViewById(R.id.btn_submit_task);
        btnSubmit.setOnClickListener(v -> submitTask());

        // Initial fetch
        loadMentees();
        loadTasksHistory();
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

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MentorTaskActivity.this,
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

    private void loadTasksHistory() {
        if (mentorId == null || mentorId.isEmpty()) {
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        AdminNetworkClient.get("/mentors/" + mentorId + "/tasks", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                pbLoading.setVisibility(View.GONE);
                try {
                    JSONArray arr = new JSONArray(jsonResponse);
                    populateTasksList(arr);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing tasks history list", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "Failed to load tasks", e);
            }
        });
    }

    private void populateTasksList(JSONArray tasks) {
        layoutLogsContainer.removeAllViews();
        layoutLogsContainer.addView(tvNoLogs);

        try {
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject task = tasks.getJSONObject(i);
                tvNoLogs.setVisibility(View.GONE);

                String taskId = task.optString("id", "");
                String title = task.optString("title", "");
                String desc = task.optString("description", "");
                String status = task.optString("status", "ASSIGNED");
                String dueDate = task.optString("dueDate", "");
                String priority = task.optString("priority", "MEDIUM");
                String logStudentId = task.optString("studentId", "");

                // Match studentName from studentList if empty
                String studentName = "Student";
                for (JSONObject stud : studentList) {
                    if (stud.optString("id", "").equals(logStudentId)) {
                        studentName = stud.optString("firstName", "") + " " + stud.optString("lastName", "");
                        break;
                    }
                }

                View view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, layoutLogsContainer, false);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(title + " [" + priority + "]");
                text1.setTextColor(getResources().getColor(R.color.text_primary));
                text1.setTextSize(13);
                text1.setTypeface(null, android.graphics.Typeface.BOLD);
                
                text2.setText("Assigned To: " + studentName + " | Due: " + dueDate + "\nStatus: " + status + " | Info: " + desc);
                text2.setTextSize(11);
                
                if (status.equalsIgnoreCase("COMPLETED")) {
                    text2.setTextColor(getResources().getColor(R.color.accent_green));
                } else if (status.equalsIgnoreCase("OVERDUE")) {
                    text2.setTextColor(getResources().getColor(R.color.google_red));
                } else {
                    text2.setTextColor(getResources().getColor(R.color.accent_purple));
                }

                // Add Dialog on click to Grade/Review or Delete Task
                final String finalTaskId = taskId;
                final String finalTitle = title;
                final String finalDesc = desc;
                final String finalStatus = status;
                final String finalFeedback = task.optString("feedback", "");

                view.setOnClickListener(v -> showGradeTaskDialog(finalTaskId, finalTitle, finalDesc, finalStatus, finalFeedback));

                view.setPadding(8, 8, 8, 8);
                layoutLogsContainer.addView(view);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rendering tasks list", e);
        }
    }

    private void submitTask() {
        if (studentList.isEmpty()) {
            Toast.makeText(this, "No mentees assigned to deploy tasks to!", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spinnerStudents.getSelectedItemPosition();
        if (pos < 0 || pos >= studentList.size()) {
            Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject student = studentList.get(pos);
        String studentDbId = student.optString("id", "");

        String titleStr = etTitle.getText().toString().trim();
        String descStr = etDescription.getText().toString().trim();
        String dueStr = etDueDate.getText().toString().trim();

        if (titleStr.isEmpty() || descStr.isEmpty() || dueStr.isEmpty()) {
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
            payload.put("studentId", studentDbId);
            payload.put("title", titleStr);
            payload.put("description", descStr);
            payload.put("dueDate", dueStr);
            payload.put("priority", priority);

            // POST /mentors/{mentorId}/tasks
            AdminNetworkClient.post("/mentors/" + mentorId + "/tasks", payload.toString(), new AdminNetworkClient.ApiCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    Toast.makeText(MentorTaskActivity.this, "Task assigned successfully!", Toast.LENGTH_SHORT).show();
                    etTitle.setText("");
                    etDescription.setText("");
                    loadTasksHistory(); // reload history list
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to deploy task", e);
                    Toast.makeText(MentorTaskActivity.this, "Failed to assign task", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error constructing task payload", e);
        }
    }

    private void showGradeTaskDialog(String taskId, String title, String description, String status, String feedback) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("Review & Grade Task: " + title);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_signin, null); // recycle signin layout or construct custom dialog layout
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 24, 32, 24);

        TextView tvDesc = new TextView(this);
        tvDesc.setText("Task Description: " + description);
        tvDesc.setTextColor(getResources().getColor(R.color.text_secondary));
        tvDesc.setPadding(0, 0, 0, 16);
        container.addView(tvDesc);

        TextView tvStatusTitle = new TextView(this);
        tvStatusTitle.setText("Status Selection:");
        tvStatusTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(tvStatusTitle);

        RadioGroup rgStatus = new RadioGroup(this);
        rgStatus.setOrientation(RadioGroup.HORIZONTAL);
        rgStatus.setPadding(0, 8, 0, 16);

        RadioButton rbAssigned = new RadioButton(this);
        rbAssigned.setText("Assigned");
        rbAssigned.setId(View.generateViewId());
        rgStatus.addView(rbAssigned);

        RadioButton rbProgress = new RadioButton(this);
        rbProgress.setText("In Progress");
        rbProgress.setId(View.generateViewId());
        rgStatus.addView(rbProgress);

        RadioButton rbCompleted = new RadioButton(this);
        rbCompleted.setText("Completed");
        rbCompleted.setId(View.generateViewId());
        rgStatus.addView(rbCompleted);

        RadioButton rbOverdue = new RadioButton(this);
        rbOverdue.setText("Overdue");
        rbOverdue.setId(View.generateViewId());
        rgStatus.addView(rbOverdue);

        // Pre-select current status
        if (status.equalsIgnoreCase("ASSIGNED")) {
            rbAssigned.setChecked(true);
        } else if (status.equalsIgnoreCase("IN_PROGRESS")) {
            rbProgress.setChecked(true);
        } else if (status.equalsIgnoreCase("COMPLETED")) {
            rbCompleted.setChecked(true);
        } else if (status.equalsIgnoreCase("OVERDUE")) {
            rbOverdue.setChecked(true);
        }

        container.addView(rgStatus);

        TextView tvFeedbackTitle = new TextView(this);
        tvFeedbackTitle.setText("Mentor Grading Feedback:");
        tvFeedbackTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(tvFeedbackTitle);

        EditText etFeedback = new EditText(this);
        etFeedback.setHint("Write evaluation comments...");
        etFeedback.setText(feedback);
        etFeedback.setPadding(8, 8, 8, 8);
        container.addView(etFeedback);

        builder.setView(container);

        builder.setPositiveButton("Save Grade", (dialog, which) -> {
            String feedbackComments = etFeedback.getText().toString().trim();
            String newStatus = "ASSIGNED";
            int checkedId = rgStatus.getCheckedRadioButtonId();
            if (checkedId == rbProgress.getId()) {
                newStatus = "IN_PROGRESS";
            } else if (checkedId == rbCompleted.getId()) {
                newStatus = "COMPLETED";
            } else if (checkedId == rbOverdue.getId()) {
                newStatus = "OVERDUE";
            }

            try {
                JSONObject payload = new JSONObject();
                payload.put("status", newStatus);
                payload.put("feedback", feedbackComments);

                // PUT /mentors/tasks/{taskId}
                AdminNetworkClient.put("/mentors/tasks/" + taskId, payload.toString(), new AdminNetworkClient.ApiCallback() {
                    @Override
                    public void onSuccess(String jsonResponse) {
                        Toast.makeText(MentorTaskActivity.this, "Task graded successfully!", Toast.LENGTH_SHORT).show();
                        loadTasksHistory();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed grading task", e);
                        Toast.makeText(MentorTaskActivity.this, "Failed to save grading feedback", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error constructing grading updates", e);
            }
        });

        builder.setNegativeButton("Delete Task", (dialog, which) -> {
            // DELETE /mentors/tasks/{taskId}
            AdminNetworkClient.delete("/mentors/tasks/" + taskId, new AdminNetworkClient.ApiCallback() {
                @Override
                public void onSuccess(String jsonResponse) {
                    Toast.makeText(MentorTaskActivity.this, "Task removed successfully!", Toast.LENGTH_SHORT).show();
                    loadTasksHistory();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to remove task", e);
                    Toast.makeText(MentorTaskActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNeutralButton("Cancel", null);
        builder.show();
    }
}
