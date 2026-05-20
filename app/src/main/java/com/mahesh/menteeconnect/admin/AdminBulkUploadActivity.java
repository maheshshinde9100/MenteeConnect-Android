package com.mahesh.menteeconnect.admin;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;
import com.mahesh.menteeconnect.R;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AdminBulkUploadActivity extends AppCompatActivity {

    private MaterialCardView cardUploadZone, cardFileStatus;
    private TextView tvFileName, tvConsoleLogs;
    private Button btnImport;

    private Uri selectedFileUri;
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_bulk_upload);

        // System inset support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize general actions
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(view -> finish());

        // Bind layout views
        cardUploadZone = findViewById(R.id.card_upload_zone);
        cardFileStatus = findViewById(R.id.card_file_status);
        tvFileName = findViewById(R.id.tv_selected_file_name);
        tvConsoleLogs = findViewById(R.id.tv_console_logs);
        btnImport = findViewById(R.id.btn_import);

        // Initialize File Picker Launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedFileUri = uri;
                        String fileName = getFileName(uri);
                        tvFileName.setText(fileName);
                        cardFileStatus.setVisibility(View.VISIBLE);
                        tvConsoleLogs.setText("Console Active.\n[INFO] Selected CSV file target: " + fileName + "\nReady to parse and upload.\n");
                        btnImport.setEnabled(true);
                        Toast.makeText(AdminBulkUploadActivity.this, "Spreadsheet loaded successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Upload zone click (Launches real system file picker)
        cardUploadZone.setOnClickListener(view -> filePickerLauncher.launch("*/*"));

        // Import execute click
        btnImport.setOnClickListener(view -> beginRealImport());
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("AdminBulkUpload", "Error querying file name", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void beginRealImport() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }
        btnImport.setEnabled(false);
        cardUploadZone.setEnabled(false);
        tvConsoleLogs.append("\n[START] Parsing CSV stream nodes...\n");

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
            if (inputStream == null) {
                tvConsoleLogs.append("[ERROR] Unable to open selected file.\n");
                btnImport.setEnabled(true);
                cardUploadZone.setEnabled(true);
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String headerLine = reader.readLine();
            if (headerLine == null) {
                tvConsoleLogs.append("[ERROR] Empty CSV file.\n");
                reader.close();
                inputStream.close();
                btnImport.setEnabled(true);
                cardUploadZone.setEnabled(true);
                return;
            }
            tvConsoleLogs.append("[INFO] Fetching headers schema mapping... [" + headerLine + "]\n");

            // Read all data rows
            List<String[]> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // split by comma keeping quoted fields together
                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (int i = 0; i < tokens.length; i++) {
                    tokens[i] = tokens[i].trim().replaceAll("^\"|\"$", ""); // strip quotes
                }
                rows.add(tokens);
            }
            reader.close();
            inputStream.close();

            if (rows.isEmpty()) {
                tvConsoleLogs.append("[ERROR] No data rows found in CSV.\n");
                btnImport.setEnabled(true);
                cardUploadZone.setEnabled(true);
                return;
            }

            // Map headers to lower case for easier comparison
            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim().toLowerCase().replaceAll("^\"|\"$", "");
            }

            importRowsSequential(headers, rows, 0, 0);

        } catch (Exception e) {
            tvConsoleLogs.append("[ERROR] Failed to read CSV: " + e.getMessage() + "\n");
            btnImport.setEnabled(true);
            cardUploadZone.setEnabled(true);
        }
    }

    private void importRowsSequential(final String[] headers, final List<String[]> rows, final int index, final int successCount) {
        if (index >= rows.size()) {
            tvConsoleLogs.append("\n[SUCCESS] Seeding operations compiled successfully!\n");
            Toast.makeText(AdminBulkUploadActivity.this, "Seeding complete! " + successCount + " of " + rows.size() + " records imported.", Toast.LENGTH_LONG).show();
            btnImport.setEnabled(true);
            cardUploadZone.setEnabled(true);
            return;
        }

        String[] row = rows.get(index);
        tvConsoleLogs.append("[INFO] Row " + (index + 1) + ": Parsing entry...\n");

        org.json.JSONObject payload = new org.json.JSONObject();
        try {
            String firstName = "";
            String lastName = "";
            String email = "";
            String username = "";
            String password = "Password123";
            String roll = "";
            String course = "";
            String batch = "";
            int semester = 1;
            String department = "";
            double cgpa = 8.0;
            double attendance = 75.0;
            String phone = "";

            for (int i = 0; i < headers.length; i++) {
                if (i >= row.length) break;
                String header = headers[i];
                String val = row[i];
                if (header.contains("first")) firstName = val;
                else if (header.contains("last")) lastName = val;
                else if (header.equals("email")) email = val;
                else if (header.equals("username")) username = val;
                else if (header.contains("password")) password = val;
                else if (header.contains("roll") || header.contains("student")) roll = val;
                else if (header.equals("course") || header.contains("branch")) course = val;
                else if (header.equals("batch") || header.contains("year")) batch = val;
                else if (header.equals("semester")) {
                    try { semester = Integer.parseInt(val); } catch (Exception ignored) {}
                }
                else if (header.equals("department") || header.equals("dept")) department = val;
                else if (header.equals("cgpa")) {
                    try { cgpa = Double.parseDouble(val); } catch (Exception ignored) {}
                }
                else if (header.equals("attendance")) {
                    try { attendance = Double.parseDouble(val); } catch (Exception ignored) {}
                }
                else if (header.contains("phone")) phone = val;
            }

            if (username.isEmpty()) {
                username = email.isEmpty() ? "user_" + System.currentTimeMillis() : (email.contains("@") ? email.split("@")[0] : email);
            }
            if (email.isEmpty()) {
                email = username + "@example.com";
            }
            if (firstName.isEmpty() && lastName.isEmpty()) {
                firstName = username;
            }
            if (roll.isEmpty()) {
                roll = "STU" + (1000 + index);
            }

            payload.put("firstName", firstName);
            payload.put("lastName", lastName);
            payload.put("email", email);
            payload.put("username", username);
            payload.put("password", password);
            payload.put("studentId", roll);
            payload.put("rollNumber", roll);
            payload.put("course", course);
            payload.put("batch", batch);
            payload.put("academicYear", batch);
            payload.put("semester", semester);
            payload.put("department", department.isEmpty() ? course : department);
            payload.put("cgpa", cgpa);
            payload.put("attendance", attendance);
            payload.put("phoneNumber", phone);
            payload.put("role", "ROLE_STUDENT");

            tvConsoleLogs.append("       Parsed: " + firstName + " " + lastName + " (" + email + ")\n");

        } catch (Exception e) {
            tvConsoleLogs.append("       [ERROR] Parse failed: " + e.getMessage() + "\n");
            importRowsSequential(headers, rows, index + 1, successCount);
            return;
        }

        final String entryName = payload.optString("firstName") + " " + payload.optString("lastName");
        AdminNetworkClient.post("/admin/students", payload.toString(), new AdminNetworkClient.ApiCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                tvConsoleLogs.append("       [SUCCESS] Row " + (index + 1) + ": '" + entryName + "' written to MongoDB!\n");
                importRowsSequential(headers, rows, index + 1, successCount + 1);
            }

            @Override
            public void onFailure(Exception e) {
                tvConsoleLogs.append("       [ERROR] Row " + (index + 1) + ": '" + entryName + "' upload failed: " + e.getMessage() + "\n");
                importRowsSequential(headers, rows, index + 1, successCount);
            }
        });
    }
}
