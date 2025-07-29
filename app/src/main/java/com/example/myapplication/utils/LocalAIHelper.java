package com.example.myapplication.utils;

import android.os.AsyncTask;
import androidx.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class LocalAIHelper {
    public interface LocalAIStatusCallback {
        void onStatus(@Nullable Boolean isActive);
    }

    public interface LocalAIResponseCallback {
        void onResponse(@Nullable String response);
    }

    /**
     * Verifică dacă serverul AI local este activ (răspunde la /health cu 200)
     * @param aiServerUrl ex: "http://192.168.x.x:5000" sau "http://localhost:5000"
     * @param callback callback cu true dacă e activ, false dacă nu
     */
    public static void checkAILocalStatus(String aiServerUrl, LocalAIStatusCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL(aiServerUrl + "/health");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(2000); // 2 secunde timeout
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        // Opțional: verifică și conținutul răspunsului
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine = in.readLine();
                        in.close();
                        return inputLine != null && inputLine.trim().equalsIgnoreCase("OK");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean isActive) {
                callback.onStatus(isActive);
            }
        }.execute();
    }

    /**
     * Trimite o întrebare către AI-ul local și primește răspunsul
     * @param aiServerUrl URL-ul serverului AI local
     * @param message întrebarea de trimis
     * @param callback callback cu răspunsul AI
     */
    public static void sendMessageToLocalAI(String aiServerUrl, String message, LocalAIResponseCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(aiServerUrl + "/chat"); // sau /generate, /ask, etc.
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000); // 10 secunde timeout pentru AI

                    // Creăm JSON-ul cu întrebarea
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("message", message);
                    jsonBody.put("user_id", "android_app"); // opțional

                    // Trimitem datele
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonBody.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Citim răspunsul
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // Încercăm să parsez JSON-ul răspunsului
                        try {
                            JSONObject jsonResponse = new JSONObject(response.toString());
                            // Adaptează aceste câmpuri în funcție de structura răspunsului tău
                            if (jsonResponse.has("response")) {
                                return jsonResponse.getString("response");
                            } else if (jsonResponse.has("answer")) {
                                return jsonResponse.getString("answer");
                            } else if (jsonResponse.has("text")) {
                                return jsonResponse.getString("text");
                            } else {
                                // Dacă nu găsim câmpul așteptat, returnăm tot răspunsul
                                return response.toString();
                            }
                        } catch (Exception e) {
                            // Dacă nu e JSON valid, returnăm răspunsul ca string
                            return response.toString();
                        }
                    } else {
                        return "Eroare server: " + responseCode;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Eroare conexiune: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                callback.onResponse(result);
            }
        }.execute();
    }
} 