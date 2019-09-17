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
import nl.cwi.dis.physiofashion.experiment.ExperimentParser;
import nl.cwi.dis.physiofashion.experiment.ExternalCondition;

/**
 * This activity is the main entry point of the app. It makes sure that the app has permission to
 * write to storage, reads the experiment configuration from the app directory and renders the UI,
 * where the user inputs their ID and external condition configuration if available. Once all
 * necessary data is collected, it starts the actual experiment.
 */
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQUEST = 1;

    private Button nextButton;
    private ToggleButton externalConditionToggle;
    private EditText participantText;
    private EditText conditionText;

    /**
     * Called when the activity is first instantiated. Checks whether the app has permission to
     * write to storage. Ask the user to give permission or loads the UI if permission has already
     * been granted.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check whether app has permission to write to storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Ask user to grant permission
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                    STORAGE_PERMISSION_REQUEST
            );
        } else {
            Log.d(LOG_TAG, "Permission already granted");
            // Load UI
            this.setupUI();
        }
    }

    /**
     * Callback that is invoked in response to a permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Is this method invoked as a response to a request to grant storage write permissions?
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            // Has the permission been granted?
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Load UI
                this.setupUI();
            } else {
                // Show dialogue and explain that the app needs storage write permissions to function
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

    /**
     * Loads the UI of the main activity and assigns all event handlers for UI elements
     */
    private void setupUI() {
        nextButton = findViewById(R.id.main_next_button);
        externalConditionToggle = findViewById(R.id.external_condition_toggle);
        participantText = findViewById(R.id.participant_text);
        conditionText = findViewById(R.id.condition_text);

        // Parse experiment and get ExperimentParser object
        ExperimentParser experimentParser = this.parseExperiment();

        // Check whether the parsed experiment exists and is valid
        if (experimentParser == null || !experimentParser.isValidExperiment()) {
            // Show dialog informing the user that the experiment file could not be found
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

        // Try to retrieve a participant ID from the intent object
        Intent intent = getIntent();
        String participantId = intent.getStringExtra("participant");

        // Pre-fill participant field with data from the intent if available
        if (participantId != null) {
            participantText.setText(participantId);
        }

        // Retrieve external condition configuration from the experiment parser
        TextView externalConditionLabel = findViewById(R.id.external_condition_label);
        ExternalCondition externalCondition = experimentParser.getExternalCondition();

        // Update external condition label and toggle if available, hide controls otherwise
        if (externalCondition != null) {
            externalConditionLabel.setText(externalCondition.getLabel());

            ArrayList<String> options = externalCondition.getOptions();
            externalConditionToggle.setTextOff(options.get(0));
            externalConditionToggle.setTextOn(options.get(1));
            externalConditionToggle.setChecked(false);
        } else {
            Log.d(LOG_TAG, "External condition not found, hiding elements");
            externalConditionToggle.setVisibility(View.GONE);
            externalConditionLabel.setVisibility(View.GONE);
        }

        // Install TextWatcher for all text fields in the UI
        this.watchTextFieldChanges();

        // Install click handler for 'Next' button
        nextButton.setOnClickListener((View v) -> {
            nextButton.setEnabled(false);
            int counterBalance = Integer.parseInt(conditionText.getText().toString().trim());

            // Initialise new Experiment object using parser and values from the UI
            Experiment experiment = new Experiment(
                    experimentParser,
                    participantText.getText().toString().trim(),
                    counterBalance,
                    (externalCondition == null) ? null : externalConditionToggle.getText().toString()
            );

            // Get hostname of thermalwear endpoint and make sure it is available
            String hostname = experimentParser.getHostname();
            this.checkHost(hostname, () -> {
                // Host responded successfully, create intent for TemperatureChangeActivity and pass experiment data
                Intent nextActivity = new Intent(this, TemperatureChangeActivity.class);
                nextActivity.putExtra("experiment", experiment);

                // Start next activity
                nextButton.setEnabled(true);
                startActivity(nextActivity);
            }, () -> {
                // Could not communicate with thermalwear endpoint, show error message
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

    /**
     * This method adds TextWatchers to the main text input fields to make sure the fields have a
     * value before the 'Next' button is enabled.
     */
    private void watchTextFieldChanges() {
        EditText[] textFields = new EditText[] { participantText, conditionText };

        for (EditText textField : textFields) {
            textField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Text in a field has changed, check whether all fields are non-empty
                    boolean allFieldsPopulated = Arrays.stream(textFields).allMatch((e) ->
                            e.getText().toString().compareTo("") != 0
                    );

                    // Enable 'Next' button is all fields are non-empty
                    nextButton.setEnabled(allFieldsPopulated);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }
    }

    @FunctionalInterface
    private interface VoidFunction {
        void apply();
    }

    /**
     * This method checks whether the given hostname is reachable and delivers the right response.
     *
     * @param hostname Hostname to check
     * @param successFunction Callback which is invoked on success
     * @param errorFunction Callback which is invoked in case of error
     */
    private void checkHost(String hostname, VoidFunction successFunction, VoidFunction errorFunction) {
        // Specific endpoint to check
        String url = hostname + "/api/temperature";

        // Create string request
        StringRequest testRequest = new StringRequest(Request.Method.GET, url, response ->
            successFunction.apply()
        , error -> {
            Log.e(LOG_TAG, "Could not communicate with host " + hostname + ": " + error);
            errorFunction.apply();
        });

        // Add request to queue
        Volley.newRequestQueue(this).add(testRequest);
    }

    /**
     * This method retrieves the experiment config from storage and attempts to parse it. It also
     * checks whether the directory for the experiment config exists and attempts to create it if
     * not. It will load and parse the first file with the the extension `.json`.
     *
     * @return An ExperimentParser object containing the loaded data or `null` if the experiment
     * directory or experiment file could not be found.
     */
    private ExperimentParser parseExperiment() {
        // Get path to external storage and experiment directory, which is a directory with the same
        // name as the app
        File storage = Environment.getExternalStorageDirectory();
        File experimentDir = new File(storage, getResources().getString(R.string.app_name) + File.separator);

        // Make sure that the directory exists and attempt to create it otherwise
        if (!experimentDir.exists()) {
            Log.d(LOG_TAG, "App directory does not exist");
            Log.d(LOG_TAG, "Attempting to create directory: " + experimentDir.mkdirs());

            return null;
        }

        // Filter all files with the extension `.json`.
        File[] jsonFiles = experimentDir.listFiles((dir, name) ->
                name.endsWith(".json")
        );

        // Return `null` if no JSON files were found in the selected directory
        if (jsonFiles.length == 0) {
            Log.d(LOG_TAG, "No experiment file found");

            return null;
        }

        // Return ExperimentParser initialised with the first JSON file
        return new ExperimentParser(jsonFiles[0]);
    }

    /**
     * Do nothing if the user presses the back button
     */
    @Override
    public void onBackPressed() {
    }
}
