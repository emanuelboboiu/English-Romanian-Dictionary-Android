package ro.pontes.englishromaniandictionary;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Runs the application's small HTTP GET requests away from the UI thread. */
final class WebDataClient {

    interface Callback {
        void onComplete(String content);
    }

    private static final String TAG = "WebDataClient";
    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 20_000;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private WebDataClient() {
    }

    static void get(String url, Callback callback) {
        EXECUTOR.execute(() -> {
            String content = read(url);
            if (callback != null) {
                MAIN_HANDLER.post(() -> callback.onComplete(content));
            }
        });
    }

    static String buildUrl(String baseUrl, String... queryParameters) {
        if (queryParameters.length % 2 != 0) {
            throw new IllegalArgumentException("Query parameters must be key-value pairs");
        }
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        for (int index = 0; index < queryParameters.length; index += 2) {
            builder.appendQueryParameter(queryParameters[index], queryParameters[index + 1]);
        }
        return builder.build().toString();
    }

    static boolean isUiContextActive(Context context) {
        if (!(context instanceof Activity)) {
            return true;
        }
        Activity activity = (Activity) context;
        return !activity.isFinishing() && !activity.isDestroyed();
    }

    private static String read(String urlText) {
        HttpURLConnection connection = null;
        StringBuilder content = new StringBuilder();
        try {
            connection = (HttpURLConnection) new URL(urlText).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
        } catch (Exception exception) {
            Log.w(TAG, "Web request failed", exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return content.toString();
    }
}
