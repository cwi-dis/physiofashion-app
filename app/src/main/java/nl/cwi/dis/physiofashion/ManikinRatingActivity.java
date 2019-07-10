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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manikin_rating);

        Intent intent = this.getIntent();
        Experiment experiment = intent.getParcelableExtra("experiment");

        final Button ratingNextButton = findViewById(R.id.rating_next_button);
        final SelfAssessmentManikin arousalScale = findViewById(R.id.manikin_arousal);
        final SelfAssessmentManikin valenceScale = findViewById(R.id.manikin_valence);

        ratingNextButton.setOnClickListener(v -> {
            UserResponse currentResponse = experiment.getCurrentUserResponse();

            currentResponse.setArousal(arousalScale.getSelectedValue());
            currentResponse.setValence(valenceScale.getSelectedValue());

            experiment.incrementCurrentTrial();
            Trial nextTrial = experiment.getCurrentTrial();

            Intent nextActivity;

            if (experiment.shouldExperimentPause()) {
                nextActivity = new Intent(this, PauseActivity.class);
            } else if (experiment.shouldExternalConditionChange()) {
                nextActivity = new Intent(this, PauseActivity.class);
                nextActivity.putExtra("noCountdown", true);
            } else if (nextTrial == null) {
                nextActivity = new Intent(this, EndingActivity.class);
            } else {
                nextActivity = new Intent(this, TemperatureChangeActivity.class);
            }

            nextActivity.putExtra("experiment", experiment);
            startActivity(nextActivity);
        });
    }

    @Override
    public void onBackPressed() {
    }
}
