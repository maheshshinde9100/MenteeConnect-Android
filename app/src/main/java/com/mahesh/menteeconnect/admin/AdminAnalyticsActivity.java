package com.mahesh.menteeconnect.admin;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.mahesh.menteeconnect.R;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private ProgressBar pbDept1, pbDept2, pbDept3;
    private ProgressBar pbCgpa1, pbCgpa2, pbCgpa3;
    private ProgressBar pbUtilization;

    // Targets percentages
    private int targetDept1 = 0;
    private int targetDept2 = 0;
    private int targetDept3 = 0;
    private int targetCgpa1 = 0;
    private int targetCgpa2 = 0;
    private int targetCgpa3 = 0;
    private int targetUtil = 0;

    private final Handler handler = new Handler();
    private int currentTick = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_analytics);

        // System inset support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize general actions
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(view -> finish());

        // Bind progress bars
        pbDept1 = findViewById(R.id.pb_dept_1);
        pbDept2 = findViewById(R.id.pb_dept_2);
        pbDept3 = findViewById(R.id.pb_dept_3);

        pbCgpa1 = findViewById(R.id.pb_cgpa_1);
        pbCgpa2 = findViewById(R.id.pb_cgpa_2);
        pbCgpa3 = findViewById(R.id.pb_cgpa_3);

        pbUtilization = findViewById(R.id.pb_utilization);

        Button btnRecalculate = findViewById(R.id.btn_recalculate);
        btnRecalculate.setOnClickListener(view -> triggerRecalculationAnims());

        // Run animation on load
        triggerRecalculationAnims();
    }

    private void triggerRecalculationAnims() {
        Toast.makeText(this, "Compiling metrics logs...", Toast.LENGTH_SHORT).show();

        // Reset to 0
        pbDept1.setProgress(0);
        pbDept2.setProgress(0);
        pbDept3.setProgress(0);
        pbCgpa1.setProgress(0);
        pbCgpa2.setProgress(0);
        pbCgpa3.setProgress(0);
        pbUtilization.setProgress(0);

        AdminNetworkClient.get("/admin/students", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String studentResponse) {
                try {
                    org.json.JSONArray students = new org.json.JSONArray(studentResponse);
                    int total = students.length();
                    int cgpaUnder7 = 0;
                    int cgpa7To85 = 0;
                    int cgpaOver85 = 0;
                    for (int i = 0; i < total; i++) {
                        org.json.JSONObject s = students.getJSONObject(i);
                        double gpa = s.optDouble("cgpa", 0.0);
                        if (gpa < 7.0) {
                            cgpaUnder7++;
                        } else if (gpa <= 8.5) {
                            cgpa7To85++;
                        } else {
                            cgpaOver85++;
                        }
                    }
                    if (total > 0) {
                        targetCgpa1 = (cgpaUnder7 * 100) / total;
                        targetCgpa2 = (cgpa7To85 * 100) / total;
                        targetCgpa3 = (cgpaOver85 * 100) / total;
                    } else {
                        targetCgpa1 = 0;
                        targetCgpa2 = 0;
                        targetCgpa3 = 0;
                    }
                } catch (Exception e) {
                    android.util.Log.e("AdminAnalytics", "Error parsing student list for CGPA stats", e);
                }
                fetchAnalyticsSummary();
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.e("AdminAnalytics", "Failed to fetch student list for CGPA stats", e);
                targetCgpa1 = 0;
                targetCgpa2 = 0;
                targetCgpa3 = 0;
                fetchAnalyticsSummary();
            }
        });
    }

    private void fetchAnalyticsSummary() {
        AdminNetworkClient.get("/admin/analytics", new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    org.json.JSONObject root = new org.json.JSONObject(jsonResponse);
                    android.util.Log.d("AdminAnalytics", "Successfully synchronized live analytical payloads!");

                    // Parse dynamic enrollment densities from departmentStats
                    org.json.JSONObject deptStats = root.optJSONObject("departmentStats");
                    if (deptStats != null) {
                        org.json.JSONObject stuPerDept = deptStats.optJSONObject("studentsPerDepartment");
                        if (stuPerDept != null) {
                            double cs = stuPerDept.optDouble("Computer Science", 0.0);
                            double it = stuPerDept.optDouble("Information Technology", 0.0);
                            double elec = stuPerDept.optDouble("Electronics", 0.0);
                            double total = cs + it + elec;
                            if (total > 0) {
                                targetDept1 = (int) ((cs / total) * 100);
                                targetDept2 = (int) ((it / total) * 100);
                                targetDept3 = (int) ((elec / total) * 100);
                            } else {
                                targetDept1 = 0;
                                targetDept2 = 0;
                                targetDept3 = 0;
                            }
                        }
                    }

                    // Parse dynamic overall slot utilization from userCounts
                    org.json.JSONObject userCounts = root.optJSONObject("userCounts");
                    if (userCounts != null) {
                        double totalStudents = userCounts.optDouble("totalStudents", 0.0);
                        double totalMentors = userCounts.optDouble("totalMentors", 0.0);
                        if (totalMentors > 0) {
                            targetUtil = (int) ((totalStudents / (totalMentors * 10.0)) * 100.0);
                            if (targetUtil > 100) targetUtil = 100;
                        } else {
                            targetUtil = 0;
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("AdminAnalytics", "Failed to parse analytics payload", e);
                }
                startAnimationTicks();
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.e("AdminAnalytics", "Failed to retrieve analytics data from server", e);
                Toast.makeText(AdminAnalyticsActivity.this, "Failed to load database metrics", Toast.LENGTH_SHORT).show();
                targetDept1 = 0;
                targetDept2 = 0;
                targetDept3 = 0;
                targetUtil = 0;
                startAnimationTicks();
            }
        });
    }

    private void startAnimationTicks() {
        currentTick = 0;
        runProgressTicks();
    }

    private void runProgressTicks() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentTick <= 100) {
                    // Tick Dept 1
                    if (currentTick <= targetDept1) pbDept1.setProgress(currentTick);
                    else pbDept1.setProgress(targetDept1);

                    // Tick Dept 2
                    if (currentTick <= targetDept2) pbDept2.setProgress(currentTick);
                    else pbDept2.setProgress(targetDept2);

                    // Tick Dept 3
                    if (currentTick <= targetDept3) pbDept3.setProgress(currentTick);
                    else pbDept3.setProgress(targetDept3);

                    // Tick CGPA 1
                    if (currentTick <= targetCgpa1) pbCgpa1.setProgress(currentTick);
                    else pbCgpa1.setProgress(targetCgpa1);

                    // Tick CGPA 2
                    if (currentTick <= targetCgpa2) pbCgpa2.setProgress(currentTick);
                    else pbCgpa2.setProgress(targetCgpa2);

                    // Tick CGPA 3
                    if (currentTick <= targetCgpa3) pbCgpa3.setProgress(currentTick);
                    else pbCgpa3.setProgress(targetCgpa3);

                    // Tick Utilization
                    if (currentTick <= targetUtil) pbUtilization.setProgress(currentTick);
                    else pbUtilization.setProgress(targetUtil);

                    currentTick += 2; // Speed multiplier
                    runProgressTicks();
                } else {
                    Toast.makeText(AdminAnalyticsActivity.this, "MongoDB analytics calculated successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        }, 15); // Fast, smooth tick interval (15ms)
    }
}
