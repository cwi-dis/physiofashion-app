package nl.cwi.dis.physiofashion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.ExternalCondition;
import nl.cwi.dis.physiofashion.experiment.JSONExperiment;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQUEST = 1;

    private Button nextButton;
    private ToggleButton externalConditionToggle;
    private EditText participantText;
    private EditText conditionText;

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
        nextButton = findViewById(R.id.main_next_button);
        externalConditionToggle = findViewById(R.id.external_condition_toggle);
        participantText = findViewById(R.id.participant_text);
        conditionText = findViewById(R.id.condition_text);

        JSONExperiment experimentData = this.getExperimentJSON();

        if (experimentData == null || !experimentData.isValidExperiment()) {
            new AlertDialog.Builder(this)
                    .setTitle("Experiment directory")
                    .setMessage("Place your experiment files into the PhysioFashion/ directory on your external storage and restart the app")
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        finishAffinity();
                        System.exit(0);
                    })
                    .show();

            return;
        }

        Intent intent = getIntent();
        String participantId = intent.getStringExtra("participant");

        if (participantId != null) {
            participantText.setText(participantId);
        }

        TextView externalConditionLabel = findViewById(R.id.external_condition_label);
        ExternalCondition externalCondition = experimentData.getExternalCondition();

        externalConditionLabel.setText(externalCondition.getLabel());

        ArrayList<String> options = externalCondition.getOptions();
        externalConditionToggle.setTextOff(options.get(0));
        externalConditionToggle.setTextOn(options.get(1));
        externalConditionToggle.setChecked(false);

        this.watchTextFieldChanges();

        nextButton.setOnClickListener((View v) -> {
            nextButton.setEnabled(false);
            boolean fabricOn = externalConditionToggle.getText() == externalConditionToggle.getTextOn();
            int counterBalance = Integer.parseInt(conditionText.getText().toString().trim());

            Experiment experiment = new Experiment(
                    experimentData,
                    participantText.getText().toString().trim(),
                    counterBalance,
                    fabricOn
            );

            String hostname = experimentData.getHostname();
            this.checkHost(hostname, () -> {
                Intent nextActivity = new Intent(this, TemperatureChangeActivity.class);
                nextActivity.putExtra("experiment", experiment);

                nextButton.setEnabled(true);
                startActivity(nextActivity);
            }, () -> {
                Toast errorToast = Toast.makeText(
                        this,
                        "Could not communicate with host " + hostname,
                        Toast.LENGTH_LONG
                );

                errorToast.show();
                nextButton.setEnabled(true);
            });
        });
    }

    private void watchTextFieldChanges() {
        EditText[] textFields = new EditText[] { participantText, conditionText };

        for (EditText textField : textFields) {
            textField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    nextButton.setEnabled(areTextFieldsPopulated());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }
    }

    private boolean areTextFieldsPopulated() {
        String participantName = participantText.getText().toString();
        String counterbalanceValue = conditionText.getText().toString();

        return participantName.compareTo("") != 0 && counterbalanceValue.compareTo("") != 0;
    }

    @FunctionalInterface
    private interface VoidFunction {
        void apply();
    }

    private void checkHost(String hostname, VoidFunction successFunction, VoidFunction errorFunction) {
        String url = hostname + "/api/temperature";
        StringRequest testRequest = new StringRequest(Request.Method.GET, url, response ->
            successFunction.apply()
        , error -> {
            Log.e(LOG_TAG, "Could not communicate with host " + hostname + ": " + error);
            errorFunction.apply();
        });

        Volley.newRequestQueue(this).add(testRequest);
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
            name.endsWith(".mp3") || name.endsWith(".wav")
        );

        Log.d(LOG_TAG, "Audio files: " + videoFiles.length);
        Arrays.sort(videoFiles);

        return new ArrayList<>(Arrays.asList(videoFiles));
    }

    private JSONExperiment getExperimentJSON() {
        File storage = Environment.getExternalStorageDirectory();
        File experimentDir = new File(storage, getResources().getString(R.string.app_name) + "/");

        if (!experimentDir.exists()) {
            Log.d(LOG_TAG, "App directory does not exist");
            Log.d(LOG_TAG, "Attempting to create directory: " + experimentDir.mkdirs());

            return null;
        }

        File[] jsonFiles = experimentDir.listFiles((dir, name) ->
                name.endsWith(".json")
        );

        if (jsonFiles.length == 0) {
            Log.d(LOG_TAG, "No experiment file found");

            return null;
        }

        return new JSONExperiment(jsonFiles[0]);
    }

    @Override
    public void onBackPressed() {
    }
}
