package id.neotica.holomarket.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by ryomartin on 14/03/26.
 */

public class ApiTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private String urlString;
    private String method; // "GET", "POST", "PUT", "DELETE"
    private String jsonPayload; // For POST/PUT data
    private ApiCallback callback;
    private ProgressDialog dialog;
    private String loadingMessage;
    private Map<String, String> headers;

    public ApiTask(Context context, String method, String urlString, String jsonPayload, String loadingMessage, ApiCallback callback) {
        this(context, method, urlString, jsonPayload, loadingMessage, callback, null);
    }

    public ApiTask(Context context, String method, String urlString, String jsonPayload, String loadingMessage, ApiCallback callback, Map<String, String> headers) {
        this.context = context;
        this.method = method;
        this.urlString = urlString;
        this.jsonPayload = jsonPayload;
        this.loadingMessage = loadingMessage;
        this.callback = callback;
        this.headers = headers;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (loadingMessage == null) {
            return;
        }
        try {
            dialog = ProgressDialog.show(context, "", loadingMessage, true);
        } catch (Throwable t) {
            // Ignore window crash
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // If we are sending data (POST or PUT), we need to write to the OutputStream
            if (("POST".equals(method) || "PUT".equals(method)) && jsonPayload != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                OutputStream os = conn.getOutputStream();
                os.write(jsonPayload.getBytes());
                os.flush();
                os.close();
            }

            // Check if request was successful (HTTP 200 or 201)
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode <= 299) {
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            } else {
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return "HTTP_ERROR_" + responseCode + "|" + response.toString();
                }
                return "HTTP_ERROR_" + responseCode;
            }

            conn.disconnect();
            return response.toString();

        } catch (Throwable e) {
            return "NETWORK_ERROR_" + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Throwable t) {
            // Ignore window crash
        }

        // Send result back to the Activity
        if (result != null && result.startsWith("HTTP_ERROR_")) {
            callback.onError(result);
        } else if (result != null && result.startsWith("NETWORK_ERROR_")) {
            callback.onError("Failed to connect. Check internet.");
        } else if (result != null) {
            callback.onSuccess(result);
        } else {
            callback.onError("Unknown error occurred.");
        }
    }
}