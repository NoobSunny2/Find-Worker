package com.example.findworker;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class GeminiPro {

    private static final String API_KEY = "AIzaSyDISOUhwJIDQeiT1Y7BvqfRC4lcsXRFh2E";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private final OkHttpClient client;

    public interface GeminiCallback {
        void onResult(String profession, boolean isBookingIntent, String message);
        void onError(String error);
    }

    public GeminiPro() {
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    // ================= TEXT AI =================
    public void detectProfession(String userText,
                                 List<String> professions,
                                 GeminiCallback callback) {

        try {

            String prompt =
                    "User problem: " + userText +
                            ". Available professions: " + professions +
                            ". Task: detect best profession. " +
                            "If user says book, intent=booking else search. " +
                            "Return JSON: {profession:'', intent:''}";

            JSONObject body = buildTextRequest(prompt);

            sendRequest(body, professions, callback);

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // ================= IMAGE AI =================
    public void detectProfessionFromImage(android.graphics.Bitmap bitmap,
                                          List<String> professions,
                                          GeminiCallback callback) {

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos);
            String base64 = android.util.Base64.encodeToString(
                    baos.toByteArray(),
                    android.util.Base64.NO_WRAP);

            String prompt =
                    "Detect profession needed from this problem image. " +
                            "Available: " + professions +
                            ". Return JSON {profession:'', intent:'search'}";

            JSONObject body = buildImageRequest(prompt, base64);

            sendRequest(body, professions, callback);

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // ================= SEND REQUEST =================
    private void sendRequest(JSONObject body,
                             List<String> professions,
                             GeminiCallback callback) {

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(RequestBody.create(
                        body.toString(),
                        MediaType.get("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws java.io.IOException {

                if (!response.isSuccessful()) {
                    callback.onError("API Error");
                    return;
                }

                try {

                    String text = new JSONObject(response.body().string())
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    text = text.replace("```json", "")
                            .replace("```", "")
                            .trim();

                    JSONObject result = new JSONObject(text);

                    String prof = result.optString("profession", "Others");
                    String intent = result.optString("intent", "search");

                    String finalProf = "Others";

                    for (String p : professions) {
                        if (p.equalsIgnoreCase(prof)) {
                            finalProf = p;
                            break;
                        }
                    }

                    boolean isBooking = intent.equalsIgnoreCase("booking");

                    callback.onResult(
                            finalProf,
                            isBooking,
                            "Showing best " + finalProf
                    );

                } catch (Exception e) {
                    callback.onError("Parse Error");
                }
            }
        });
    }

    // ================= JSON BUILDERS =================
    private JSONObject buildTextRequest(String prompt) throws Exception {

        JSONArray parts = new JSONArray()
                .put(new JSONObject().put("text", prompt));

        return new JSONObject()
                .put("contents",
                        new JSONArray()
                                .put(new JSONObject().put("parts", parts)));
    }

    private JSONObject buildImageRequest(String prompt,
                                         String base64) throws Exception {

        JSONArray parts = new JSONArray()
                .put(new JSONObject().put("text", prompt))
                .put(new JSONObject().put("inline_data",
                        new JSONObject()
                                .put("mime_type", "image/jpeg")
                                .put("data", base64)));

        return new JSONObject()
                .put("contents",
                        new JSONArray()
                                .put(new JSONObject().put("parts", parts)));
    }
}