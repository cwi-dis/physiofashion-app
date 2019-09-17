package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.UserResponse;

/**
 * This activity is the final activity, displayed to the user after all trials have been completed.
 * It attempts to write the user responses gathered during the trials to the device's download
 * folder and displays a message if successful. It also features a button which allows the user to
 * return to the first activity (e.g. for starting another round of trials) and pass the current
 * participant ID to it, so it does not need to be filled in again.
 */
public class EndingActivity extends AppCompatActivity {
    public static final String LOG_TAG = "EndingActivity";

    /**
     * Sets up the UI and installs events handlers for UI elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ending);

        // Get experiment data from intent and log user responses to system log
        Intent intent = this.getIntent();
        Experiment experiment = intent.getParcelableExtra("experiment");
        this.logResponses(experiment.getResponses());

        // Get system's download directory
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        // Attempt to write user responses to download directory
        String savePath = experiment.writeResponsesToFile(downloadDir);

        // Check whether user responses were written to file successfully
        if (savePath != null) {
            Toast.makeText(this, "File successfully written: " + savePath, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Could not save experiment file", Toast.LENGTH_LONG).show();
        }


        // Install handler for button which returns to main entry point
        final Button returnToStart = findViewById(R.id.return_to_start);
        returnToStart.setOnClickListener(v -> {
            // Pass participant ID to MainActivity via intent
            Intent mainActivity = new Intent(this, MainActivity.class);
            mainActivity.putExtra("participant", experiment.getParticipantId());

            // Launch activity
            startActivity(mainActivity);
        });
    }

    /**
     * Logs user responses from experiment config to the system log
     * @param responses An ArrayList of UserResponse objects to log
     */
    private void logResponses(ArrayList<UserResponse> responses) {
        for (UserResponse response : responses) {
            String formattedResponse = String.format(
                    Locale.ENGLISH,
                    "%.2f, %.2f, %d, %d",
                    response.getStimulusStarted(),
                    response.getStimulusFelt(),
                    response.getTemperatureFelt(),
                    response.getComfortLevel()
            );

            Log.d(LOG_TAG, formattedResponse);
        }
    }

    /**
     * Do nothing if the user presses the back button
     */
    @Override
    public void onBackPressed() {
    }
}
