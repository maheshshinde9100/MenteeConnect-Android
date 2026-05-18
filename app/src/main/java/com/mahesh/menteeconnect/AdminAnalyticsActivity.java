package com.mahesh.menteeconnect;

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

public class AdminAnalyticsActivity extends AppCompatActivity {

    private ProgressBar pbDept1, pbDept2, pbDept3;
    private ProgressBar pbCgpa1, pbCgpa2, pbCgpa3;
    private ProgressBar pbUtilization;

    // Targets percentages
    private final int targetDept1 = 45;
    private final int targetDept2 = 38;
    private final int targetDept3 = 17;
    private final int targetCgpa1 = 22;
    private final int targetCgpa2 = 58;
    private final int targetCgpa3 = 20;
    private final int targetUtil = 56;

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

    // Micro animation loader engine
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
