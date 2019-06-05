package nl.cwi.dis.physiofashion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import nl.cwi.dis.physiofashion.experiment.Trial;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                    STORAGE_PERMISSION_REQUEST
            );
        } else {
            Log.d(LOG_TAG, "Permission already granted");
            this.setupUI();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.setupUI();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("File system permissions")
                        .setMessage("The app needs access to external storage in order to function properly. Please restart the app and grant the permission.")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            finishAffinity();
                            System.exit(0);
                        })
                        .show();
            }
        }
    }

    private void setupUI() {

    }

    private ArrayList<File> readAudioFiles() {
        File storage = Environment.getExternalStorageDirectory();
        File audioDir = new File(storage, getResources().getString(R.string.app_name) + "/");

        if (!audioDir.exists()) {
            Log.d(LOG_TAG, "App directory does not exist");
            Log.d(LOG_TAG, "Attempting to create directory: " + audioDir.mkdirs());

            return new ArrayList<>();
        }

        File[] videoFiles = audioDir.listFiles((dir, name) ->
            name.endsWith(".mp4") || name.endsWith(".wav")
        );

        Log.d(LOG_TAG, "Video files: " + videoFiles.length);
        Arrays.sort(videoFiles);

        return new ArrayList<>(Arrays.asList(videoFiles));
    }

    private JSONObject getExperimentJSON() {
        File storage = Environment.getExternalStorageDirectory();
        File experimentDir = new File(storage, getResources().getString(R.string.app_name) + "/");

        if (!experimentDir.exists()) {
            Log.d(LOG_TAG, "App directory does not exist");
            Log.d(LOG_TAG, "Attempting to create directory: " + experimentDir.mkdirs());

            return new JSONObject();
        }

        File[] jsonFiles = experimentDir.listFiles((dir, name) ->
                name.endsWith(".json")
        );

        if (jsonFiles.length == 0) {
            Log.d(LOG_TAG, "No experiment file found");

            return new JSONObject();
        }

        try {
            File experimentFile = jsonFiles[0];
            Scanner scanner = new Scanner(experimentFile);
            String fileContents = scanner.next();

            return new JSONObject(fileContents);
        } catch (FileNotFoundException fnf) {
            Log.e(LOG_TAG, "Experiment file not found: " + fnf);
            return new JSONObject();
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Could not parse JSON object: " + je);
            return new JSONObject();
        }
    }

    private ArrayList<Trial> parseExperimentData() {
        try {
            JSONObject experiment = this.getExperimentJSON();
            JSONArray trials = experiment.getJSONArray("trials");

            ArrayList<Trial> trialsList = new ArrayList<>(trials.length());

            for (int i=0; i<trials.length(); i++) {
                JSONObject trialObject = (JSONObject) trials.get(i);

                trialsList.add(new Trial(
                        trialObject.optString("audio", null),
                        trialObject.getString("condition"),
                        trialObject.getString("intensity")
                ));
            }

            return trialsList;
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Could not parse JSON: " + je);
            return new ArrayList<>();
        }
    }
}
