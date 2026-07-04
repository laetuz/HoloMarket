package id.neotica.holomarket.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ryomartin on 21/03/26.
 */

public class DownloadTask extends AsyncTask<String, Integer, String> {
    private Context context;
    private ProgressDialog progressDialog;
    private String fileName;
    private String displayName;

    public DownloadTask(Context context, String fileName) {
        this(context, fileName, null);
    }

    public DownloadTask(Context context, String fileName, String displayName) {
        this.context = context;
        this.fileName = fileName;
        this.displayName = displayName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Create a classic horizontal progress bar
        progressDialog = new ProgressDialog(context);
        String name = displayName != null ? displayName : fileName;
        progressDialog.setMessage("Downloading " + name + "...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false); // Force them to wait!
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... urls) {
        InputStream input = null;
        FileOutputStream output = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urls[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode();
            }

            int fileLength = connection.getContentLength();

            // 1. Create a NeoStore folder on the SD Card
            File downloadDir = new File(Environment.getExternalStorageDirectory(), "NeoStore");
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }

            // 2. Prepare the file
            File outputFile = new File(downloadDir, fileName);
            input = connection.getInputStream();
            output = new FileOutputStream(outputFile);

            // 3. Download the file in 4KB chunks
            byte data[] = new byte[4096];
            long total = 0;
            int count;

            while ((count = input.read(data)) != -1) {
                total += count;
                // Publish the progress to the UI thread
                if (fileLength > 0) {
                    publishProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }

            // Return the exact path where we saved it
            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (Exception ignored) { }
            if (connection != null) connection.disconnect();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // Update the loading bar
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        // Hide the dialog safely
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) { }

        if (result != null && result.startsWith("Error")) {
            Toast.makeText(context, "Download failed: " + result, Toast.LENGTH_LONG).show();
        } else if (result != null) {
            Toast.makeText(context, "Download complete!", Toast.LENGTH_SHORT).show();
            // Automatically launch the installer!
            installApk(result);
        }
    }

    // The magic method to trigger the Android Package Installer
    private void installApk(String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Tell Android exactly what kind of file this is
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to open installer.", Toast.LENGTH_LONG).show();
        }
    }
}
