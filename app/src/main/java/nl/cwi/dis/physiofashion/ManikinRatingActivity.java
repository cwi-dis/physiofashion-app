package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import java.util.Arrays;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.Trial;
import nl.cwi.dis.physiofashion.experiment.UserResponse;
import nl.cwi.dis.physiofashion.views.SelfAssessmentManikin;

public class ManikinRatingActivity extends AppCompatActivity {
    private Button ratingNextButton;
    private SelfAssessmentManikin arousalScale;
    private SelfAssessmentManikin valenceScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manikin_rating);

        Intent intent = this.getIntent();
        Experiment experiment = intent.getParcelableExtra("experiment");

        ratingNextButton = findViewById(R.id.rating_next_button);
        ratingNextButton.setEnabled(false);

        arousalScale = findViewById(R.id.manikin_arousal);
        valenceScale = findViewById(R.id.manikin_valence);

        this.watchScaleChanges();

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

    private void watchScaleChanges() {
        SelfAssessmentManikin[] scales = new SelfAssessmentManikin[] { arousalScale, valenceScale };

        for (SelfAssessmentManikin scale : scales) {
            scale.onValueSelected(value -> {
                boolean allScalesHaveValue = Arrays.stream(scales).allMatch(e -> e.getSelectedValue() > -1);
                ratingNextButton.setEnabled(allScalesHaveValue);
            });
        }
    }

    @Override
    public void onBackPressed() {
    }
}
