package com.mahesh.menteeconnect.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mahesh.menteeconnect.R;
import com.mahesh.menteeconnect.admin.AdminNetworkClient;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MentorDashboardActivity extends AppCompatActivity {

    private static final String TAG = "MentorDashboard";

    private TextView tvMentorAvatar, tvMentorName, tvMentorDesignation, tvMentorDepartment, tvMentorCapacity, tvMenteeCount;
    private LinearProgressIndicator progressCapacity;
    private ProgressBar pbLoading;
    private View layoutEmptyMentees;
    private RecyclerView rvMentees;
    
    private String mentorId = "";
    private String mentorName = "";
    private String mentorDept = "";
    private List<JSONObject> menteeList = new ArrayList<>();
    private MenteeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_dashboard);

        // Initialize UI Elements
        tvMentorAvatar = findViewById(R.id.tv_mentor_avatar);
        tvMentorName = findViewById(R.id.tv_mentor_name);
        tvMentorDesignation = findViewById(R.id.tv_mentor_designation);
        tvMentorDepartment = findViewById(R.id.tv_mentor_department);
        tvMentorCapacity = findViewById(R.id.tv_mentor_capacity);
        tvMenteeCount = findViewById(R.id.tv_mentee_count);
        progressCapacity = findViewById(R.id.progress_mentor_capacity);
        pbLoading = findViewById(R.id.pb_loading);
        layoutEmptyMentees = findViewById(R.id.layout_empty_mentees);
        rvMentees = findViewById(R.id.rv_mentees);

        rvMentees.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MenteeAdapter(menteeList);
        rvMentees.setAdapter(adapter);

        // Quick Actions
        CardView cvAttendance = findViewById(R.id.cv_action_attendance);
        CardView cvTasks = findViewById(R.id.cv_action_tasks);
        CardView cvAnnouncements = findViewById(R.id.cv_action_announcements);

        cvAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(MentorDashboardActivity.this, MentorAttendanceActivity.class);
            intent.putExtra("mentorId", mentorId);
            startActivity(intent);
        });

        cvTasks.setOnClickListener(v -> {
            Intent intent = new Intent(MentorDashboardActivity.this, MentorTaskActivity.class);
            intent.putExtra("mentorId", mentorId);
            startActivity(intent);
        });

        cvAnnouncements.setOnClickListener(v -> {
            Intent intent = new Intent(MentorDashboardActivity.this, MentorAnnouncementActivity.class);
            intent.putExtra("mentorId", mentorId);
            startActivity(intent);
        });

        ImageButton btnRefresh = findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(v -> loadMentorProfile());

        // Initial Load
        loadMentorProfile();
    }

    private void loadMentorProfile() {
        pbLoading.setVisibility(View.VISIBLE);
        layoutEmptyMentees.setVisibility(View.GONE);
        rvMentees.setVisibility(View.GONE);

        AdminNetworkClient.get("/mentors/me", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    JSONObject mentor = new JSONObject(jsonResponse);
                    mentorId = mentor.optString("id", "");
                    
                    String firstName = mentor.optString("firstName", "");
                    String lastName = mentor.optString("lastName", "");
                    mentorName = firstName + " " + lastName;
                    tvMentorName.setText(mentorName);
                    
                    String designation = mentor.optString("designation", "Academic Advisor");
                    if (designation.isEmpty() || designation.equals("null")) {
                        designation = "Academic Advisor";
                    }
                    tvMentorDesignation.setText(designation);
                    
                    mentorDept = mentor.optString("department", "Department");
                    tvMentorDepartment.setText(mentorDept);

                    // Set Avatar text
                    if (!firstName.isEmpty()) {
                        tvMentorAvatar.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
                    } else if (!mentorName.isEmpty()) {
                        tvMentorAvatar.setText(String.valueOf(mentorName.charAt(0)).toUpperCase());
                    }

                    // Capacity & Slot utilization
                    JSONArray assigned = mentor.optJSONArray("assignedStudentIds");
                    int maxStudents = mentor.optInt("maxStudents", 15);
                    int assignedCount = (assigned != null) ? assigned.length() : 0;
                    
                    tvMentorCapacity.setText(assignedCount + " / " + maxStudents + " slots");
                    int percent = (int) (((double) assignedCount / maxStudents) * 100);
                    progressCapacity.setProgress(percent);

                    // Now load assigned Mentees
                    loadAssignedMentees();
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing mentor profile", e);
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MentorDashboardActivity.this, "Failed to parse profile details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load mentor profile", e);
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MentorDashboardActivity.this, "Connection Error: Unable to fetch profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAssignedMentees() {
        if (mentorId.isEmpty()) {
            pbLoading.setVisibility(View.GONE);
            return;
        }

        AdminNetworkClient.get("/mentors/" + mentorId + "/students", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                pbLoading.setVisibility(View.GONE);
                try {
                    menteeList.clear();
                    JSONArray arr = new JSONArray(jsonResponse);
                    for (int i = 0; i < arr.length(); i++) {
                        menteeList.add(arr.getJSONObject(i));
                    }
                    
                    tvMenteeCount.setText(menteeList.size() + " Students");

                    if (menteeList.isEmpty()) {
                        layoutEmptyMentees.setVisibility(View.VISIBLE);
                        rvMentees.setVisibility(View.GONE);
                    } else {
                        layoutEmptyMentees.setVisibility(View.GONE);
                        rvMentees.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing mentees array", e);
                    layoutEmptyMentees.setVisibility(View.VISIBLE);
                    rvMentees.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "Failed to fetch mentees", e);
                layoutEmptyMentees.setVisibility(View.VISIBLE);
                rvMentees.setVisibility(View.GONE);
                Toast.makeText(MentorDashboardActivity.this, "Failed to load assigned mentees", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Recycler View Adapter for Mentees ---
    private class MenteeAdapter extends RecyclerView.Adapter<MenteeAdapter.MenteeViewHolder> {

        private final List<JSONObject> list;

        public MenteeAdapter(List<JSONObject> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MenteeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mentee, parent, false);
            return new MenteeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenteeViewHolder holder, int position) {
            try {
                JSONObject student = list.get(position);
                String studentDbId = student.optString("id", "");
                String studentId = student.optString("studentId", "");
                String firstName = student.optString("firstName", "");
                String lastName = student.optString("lastName", "");
                String fullName = firstName + " " + lastName;
                String batch = student.optString("batch", "2023-2027");
                String course = student.optString("course", "Academic Course");
                double attendance = student.optDouble("attendance", 0.0);
                double cgpa = student.optDouble("cgpa", 0.0);

                holder.tvName.setText(fullName);
                holder.tvIdBatch.setText("ID: " + studentId + " • Batch " + batch);
                holder.tvCourse.setText(course);
                holder.tvAttendance.setText(String.format("%.1f%% Attd", attendance));
                holder.tvCgpa.setText(String.format("%.2f CGPA", cgpa));

                if (!firstName.isEmpty()) {
                    holder.tvAvatar.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
                } else if (!fullName.isEmpty()) {
                    holder.tvAvatar.setText(String.valueOf(fullName.charAt(0)).toUpperCase());
                }

                // Click listener to view student details
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(MentorDashboardActivity.this, MenteeDetailActivity.class);
                    intent.putExtra("mentorId", mentorId);
                    intent.putExtra("studentId", studentDbId);
                    intent.putExtra("studentRollId", studentId);
                    intent.putExtra("studentName", fullName);
                    intent.putExtra("studentCourse", course);
                    intent.putExtra("studentBatch", batch);
                    intent.putExtra("studentEmail", student.optString("email", ""));
                    intent.putExtra("studentPhone", student.optString("phoneNumber", ""));
                    intent.putExtra("studentAttendance", attendance);
                    intent.putExtra("studentCgpa", cgpa);
                    startActivity(intent);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error binding mentee view holder", e);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MenteeViewHolder extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvName, tvIdBatch, tvCourse, tvAttendance, tvCgpa;

            public MenteeViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAvatar = itemView.findViewById(R.id.tv_student_avatar);
                tvName = itemView.findViewById(R.id.tv_student_name);
                tvIdBatch = itemView.findViewById(R.id.tv_student_id_batch);
                tvCourse = itemView.findViewById(R.id.tv_student_course);
                tvAttendance = itemView.findViewById(R.id.tv_student_attendance);
                tvCgpa = itemView.findViewById(R.id.tv_student_cgpa);
            }
        }
    }
}
