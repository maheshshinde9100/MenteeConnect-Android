package com.mahesh.menteeconnect.admin;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pure-Java thread-safe asynchronous REST client for MenteeConnect Admin APIs.
 * Communicates directly with: https://menteeconnect-backend.onrender.com
 */
public class AdminNetworkClient {

    private static final String TAG = "AdminNetworkClient";
    public static final String BASE_URL = com.mahesh.menteeconnect.BuildConfig.BASE_URL;

    // Global session JWT holder
    private static String authToken = null;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback {
        void onSuccess(String jsonResponse);
        void onFailure(Exception e);
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    /**
     * Executes asynchronous GET request.
     */
    public static void get(final String path, final ApiCallback callback) {
        executorService.execute(() -> {
            try {
                String fullUrl = BASE_URL + path;
                Log.d(TAG, "GET Request to: " + fullUrl);
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                
                // Set JWT authorization headers if session exists
                if (authToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + authToken);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "GET Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    final String result = response.toString();
                    mainHandler.post(() -> callback.onSuccess(result));
                } else {
                    throw new Exception("HTTP GET Error code: " + responseCode + " - " + conn.getResponseMessage());
                }
            } catch (final Exception e) {
                Log.e(TAG, "GET Failed", e);
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    /**
     * Executes asynchronous POST request.
     */
    public static void post(final String path, final String jsonBody, final ApiCallback callback) {
        executorService.execute(() -> {
            try {
                String fullUrl = BASE_URL + path;
                Log.d(TAG, "POST Request to: " + fullUrl + " | Body: " + jsonBody);
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                if (authToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + authToken);
                }

                if (jsonBody != null) {
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "POST Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK || 
                    responseCode == HttpURLConnection.HTTP_CREATED || 
                    responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    final String result = response.toString();
                    mainHandler.post(() -> callback.onSuccess(result));
                } else {
                    throw new Exception("HTTP POST Error code: " + responseCode + " - " + conn.getResponseMessage());
                }
            } catch (final Exception e) {
                Log.e(TAG, "POST Failed", e);
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    /**
     * Executes asynchronous PUT request.
     */
    public static void put(final String path, final String jsonBody, final ApiCallback callback) {
        executorService.execute(() -> {
            try {
                String fullUrl = BASE_URL + path;
                Log.d(TAG, "PUT Request to: " + fullUrl + " | Body: " + jsonBody);
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                if (authToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + authToken);
                }

                if (jsonBody != null) {
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "PUT Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    final String result = response.toString();
                    mainHandler.post(() -> callback.onSuccess(result));
                } else {
                    throw new Exception("HTTP PUT Error code: " + responseCode + " - " + conn.getResponseMessage());
                }
            } catch (final Exception e) {
                Log.e(TAG, "PUT Failed", e);
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    /**
     * Executes asynchronous DELETE request.
     */
    public static void delete(final String path, final ApiCallback callback) {
        executorService.execute(() -> {
            try {
                String fullUrl = BASE_URL + path;
                Log.d(TAG, "DELETE Request to: " + fullUrl);
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Accept", "application/json");

                if (authToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + authToken);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "DELETE Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK || 
                    responseCode == HttpURLConnection.HTTP_NO_CONTENT ||
                    responseCode == 204) {
                    
                    mainHandler.post(() -> callback.onSuccess("{\"success\":true,\"message\":\"Deleted successfully\"}"));
                } else {
                    throw new Exception("HTTP DELETE Error code: " + responseCode + " - " + conn.getResponseMessage());
                }
            } catch (final Exception e) {
                Log.e(TAG, "DELETE Failed", e);
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }
}
