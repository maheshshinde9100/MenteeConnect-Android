package com.mahesh.menteeconnect.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.mahesh.menteeconnect.R;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AdminMentorAllocationActivity extends AppCompatActivity {

    private TextView tvSelectedMentorName, tvMentorSlotsCapacity;
    private LinearLayout layoutSlotsDisplay, layoutStudentsContainer;
    
    private JSONArray mentorsArray = new JSONArray();
    private JSONArray studentsArray = new JSONArray();
    private int selectedMentorIndex = -1;

    private final List<CheckBox> studentCheckBoxes = new ArrayList<>();
    private final List<String> studentIdsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_mentor_allocation);

        // System inset support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize general actions
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(view -> finish());

        // Bind layouts
        LinearLayout layoutSelect = findViewById(R.id.layout_select_mentor);
        tvSelectedMentorName = findViewById(R.id.tv_selected_mentor_name);
        tvMentorSlotsCapacity = findViewById(R.id.tv_mentor_slots_capacity);
        layoutSlotsDisplay = findViewById(R.id.layout_slots_display);
        layoutStudentsContainer = findViewById(R.id.layout_students_container);
        Button btnAllocate = findViewById(R.id.btn_allocate);

        // Click to choose mentor
        layoutSelect.setOnClickListener(view -> triggerMentorSelection());

        // Fetch live database assets
        loadMentorsData();
        loadStudentsData();

        // Pair allocator click
        btnAllocate.setOnClickListener(view -> {
            if (selectedMentorIndex == -1) {
                Toast.makeText(AdminMentorAllocationActivity.this, "Please choose a Faculty Advisor Mentor first!", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject selectedMentor = mentorsArray.optJSONObject(selectedMentorIndex);
            if (selectedMentor == null) return;

            String mentorDbId = selectedMentor.optString("id", "");
            String mentorName = selectedMentor.optString("firstName", "") + " " + selectedMentor.optString("lastName", "");
            int max = selectedMentor.optInt("maxStudents", 15);
            
            JSONArray assignedArr = selectedMentor.optJSONArray("assignedStudentIds");
            int utilized = assignedArr != null ? assignedArr.length() : 0;
            int available = max - utilized;

            // Collect selected students
            List<String> selectedIds = new ArrayList<>();
            for (int i = 0; i < studentCheckBoxes.size(); i++) {
                if (studentCheckBoxes.get(i).isChecked()) {
                    selectedIds.add(studentIdsList.get(i));
                }
            }

            if (selectedIds.isEmpty()) {
                Toast.makeText(AdminMentorAllocationActivity.this, "Please choose at least one student to assign!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedIds.size() > available) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(AdminMentorAllocationActivity.this)
                        .setTitle("Capacity Limit Overload")
                        .setMessage("Warning: You selected " + selectedIds.size() + " students, but " + mentorName +
                                " only has " + available + " slot(s) remaining. Please select fewer students or choose another mentor.")
                        .setPositiveButton("Dismiss", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }

            // Construct REST assign-mentor payload
            try {
                JSONObject payload = new JSONObject();
                payload.put("mentorId", mentorDbId);
                
                JSONArray idsArray = new JSONArray();
                for (String id : selectedIds) {
                    idsArray.put(id);
                }
                payload.put("studentIds", idsArray);

                Toast.makeText(AdminMentorAllocationActivity.this, "Assigning student allocations...", Toast.LENGTH_SHORT).show();

                AdminNetworkClient.post("/admin/assign-mentor", payload.toString(), new AdminNetworkClient.ApiCallback() {
                    @Override
                    public void onSuccess(String jsonResponse) {
                        Toast.makeText(AdminMentorAllocationActivity.this, "Allocations updated successfully in MongoDB!", Toast.LENGTH_LONG).show();
                        
                        // Clear selected mentor index and reload fresh updates
                        selectedMentorIndex = -1;
                        tvSelectedMentorName.setText("Choose Mentor...");
                        layoutSlotsDisplay.setVisibility(View.GONE);
                        
                        loadMentorsData();
                        loadStudentsData();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        android.util.Log.e("AdminMentorAllocation", "Failed allocating mentor", e);
                        Toast.makeText(AdminMentorAllocationActivity.this, "Connection Error: Failed to execute allocations", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception ex) {
                android.util.Log.e("AdminMentorAllocation", "Error building payload", ex);
            }
        });
    }

    private void loadMentorsData() {
        AdminNetworkClient.get("/admin/mentors?page=0&size=50", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    JSONObject root = new JSONObject(jsonResponse);
                    mentorsArray = root.optJSONArray("content");
                    if (mentorsArray == null) {
                        mentorsArray = new JSONArray();
                    }
                } catch (Exception e) {
                    android.util.Log.e("AdminMentorAllocation", "Failed loading mentors array", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.e("AdminMentorAllocation", "Failed to retrieve mentors", e);
            }
        });
    }

    private void loadStudentsData() {
        AdminNetworkClient.get("/admin/students", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    studentsArray = new JSONArray(jsonResponse);
                    populateStudentsList();
                } catch (Exception e) {
                    android.util.Log.e("AdminMentorAllocation", "Failed loading students list", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.e("AdminMentorAllocation", "Failed to retrieve students roster", e);
            }
        });
    }

    private void populateStudentsList() {
        layoutStudentsContainer.removeAllViews();
        studentCheckBoxes.clear();
        studentIdsList.clear();

        if (studentsArray.length() == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No students currently registered in system.");
            tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary));
            tvEmpty.setPadding(8, 8, 8, 8);
            layoutStudentsContainer.addView(tvEmpty);
            return;
        }

        for (int i = 0; i < studentsArray.length(); i++) {
            try {
                JSONObject student = studentsArray.getJSONObject(i);
                String dbId = student.optString("id", "");
                String roll = student.optString("studentId", student.optString("rollNumber", ""));
                String name = student.optString("firstName", "") + " " + student.optString("lastName", "");
                String mentorName = student.optString("mentorName", "");

                CheckBox cb = new CheckBox(this);
                cb.setText(name + " (" + roll + ")");
                cb.setTextColor(getResources().getColor(R.color.text_primary));
                cb.setTextSize(14);
                cb.setPadding(0, 12, 0, 12);

                if (mentorName != null && !mentorName.isEmpty() && !mentorName.equalsIgnoreCase("null")) {
                    cb.setText(name + " (" + roll + ") [Mentor: " + mentorName + "]");
                    cb.setTextColor(getResources().getColor(R.color.text_secondary));
                }

                layoutStudentsContainer.addView(cb);
                studentCheckBoxes.add(cb);
                studentIdsList.add(dbId);

            } catch (Exception e) {
                android.util.Log.e("AdminMentorAllocation", "Error rendering checkbox", e);
            }
        }
    }

    private void triggerMentorSelection() {
        if (mentorsArray == null || mentorsArray.length() == 0) {
            Toast.makeText(this, "No mentors loaded. Please sync server.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = new String[mentorsArray.length()];
        for (int i = 0; i < mentorsArray.length(); i++) {
            JSONObject mentor = mentorsArray.optJSONObject(i);
            String name = mentor.optString("firstName", "") + " " + mentor.optString("lastName", "");
            int max = mentor.optInt("maxStudents", 15);
            JSONArray assignedArr = mentor.optJSONArray("assignedStudentIds");
            int utilized = assignedArr != null ? assignedArr.length() : 0;
            int available = max - utilized;
            options[i] = name + " (" + available + " slots available)";
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Select Faculty Advisor Mentor")
                .setItems(options, (dialog, index) -> {
                    selectedMentorIndex = index;
                    JSONObject mentor = mentorsArray.optJSONObject(index);
                    String name = mentor.optString("firstName", "") + " " + mentor.optString("lastName", "");
                    tvSelectedMentorName.setText(name);

                    int max = mentor.optInt("maxStudents", 15);
                    JSONArray assignedArr = mentor.optJSONArray("assignedStudentIds");
                    int utilized = assignedArr != null ? assignedArr.length() : 0;
                    int available = max - utilized;

                    tvMentorSlotsCapacity.setText(utilized + " / " + max + " Utilized (" + available + " available)");
                    layoutSlotsDisplay.setVisibility(View.VISIBLE);

                    if (available <= 1) {
                        tvMentorSlotsCapacity.setTextColor(getColor(R.color.google_red));
                    } else if (available <= 5) {
                        tvMentorSlotsCapacity.setTextColor(getColor(R.color.accent_orange));
                    } else {
                        tvMentorSlotsCapacity.setTextColor(getColor(R.color.accent_green));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
