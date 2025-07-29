package com.example.myapplication.utils;

import android.os.AsyncTask;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenAIHelper {
    // TODO: Înlocuiește cu cheia ta de API OpenAI
    private static final String OPENAI_API_KEY = "PUNE_CHEIA_TA_AICI";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public interface OpenAIResponseCallback {
        void onResponse(@Nullable String response);
    }

    /**
     * Trimite un mesaj la OpenAI ChatGPT și primește răspunsul (chatbot educațional)
     */
    public static void sendChatMessage(String userMessage, OpenAIResponseCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(OPENAI_API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("model", "gpt-3.5-turbo");
                    JSONArray messages = new JSONArray();
                    JSONObject userMsg = new JSONObject();
                    userMsg.put("role", "user");
                    userMsg.put("content", userMessage);
                    messages.put(userMsg);
                    jsonBody.put("messages", messages);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonBody.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        if (choices.length() > 0) {
                            return choices.getJSONObject(0).getJSONObject("message").getString("content");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                callback.onResponse(result);
            }
        }.execute();
    }

    /**
     * Generează o întrebare de quiz pe baza unui subiect dat
     */
    public static void generateQuizQuestion(String topic, OpenAIResponseCallback callback) {
        String prompt = "Generează o întrebare de quiz cu 4 variante de răspuns despre: " + topic + ". Răspunsul corect să fie marcat clar.";
        sendChatMessage(prompt, callback);
    }
} 