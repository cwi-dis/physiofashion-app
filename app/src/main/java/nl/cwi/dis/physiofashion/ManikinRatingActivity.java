package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.Trial;
import nl.cwi.dis.physiofashion.experiment.UserResponse;
import nl.cwi.dis.physiofashion.views.SelfAssessmentManikin;

public class ManikinRatingActivity extends AppCompatActivity {
    /**
     * Sets up the UI and installs events handlers for UI elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manikin_rating);

        // Get experiment data from intent
        Intent intent = this.getIntent();
        Experiment experiment = intent.getParcelableExtra("experiment");

        final Button ratingNextButton = findViewById(R.id.rating_next_button);
        final SelfAssessmentManikin arousalScale = findViewById(R.id.manikin_arousal);
        final SelfAssessmentManikin valenceScale = findViewById(R.id.manikin_valence);

        // Install click handler for 'Next' button. Gathers data from the UI and launches next
        // activity
        ratingNextButton.setOnClickListener(v -> {
            // Get user response object and set values to data gathered from the UI
            UserResponse currentResponse = experiment.getCurrentUserResponse();

            // Set arousal and valence indicated by the user
            currentResponse.setArousal(arousalScale.getSelectedValue());
            currentResponse.setValence(valenceScale.getSelectedValue());

            // Increment current trial index
            experiment.incrementCurrentTrial();
            // Get next trial
            Trial nextTrial = experiment.getCurrentTrial();

            Intent nextActivity;

            // Check whether experiment should pause
            if (experiment.shouldExperimentPause()) {
                // Launch PauseActivity with countdown
                nextActivity = new Intent(this, PauseActivity.class);
            } else if (experiment.shouldExternalConditionChange()) {
                // If external condition needs to be changed, launch PauseActivity without countdown
                nextActivity = new Intent(this, PauseActivity.class);
                nextActivity.putExtra("noCountdown", true);
            } else if (nextTrial == null) {
                // if next trial is null, we're at the end of the experiment, launch EndingActivity
                nextActivity = new Intent(this, EndingActivity.class);
            } else {
                // otherwise, launch TemperatureChangeActivity with next trial
                nextActivity = new Intent(this, TemperatureChangeActivity.class);
            }

            // Pass experiment to intent and launch next activity
            nextActivity.putExtra("experiment", experiment);
            startActivity(nextActivity);
        });
    }

    /**
     * Do nothing if the user presses the back button
     */
    @Override
    public void onBackPressed() {
    }
}
