package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.Trial;
import nl.cwi.dis.physiofashion.experiment.UserResponse;

public class RatingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        Intent intent = this.getIntent();
        Experiment experiment = intent.getParcelableExtra("experiment");

        final SeekBar temperatureSeekBar = findViewById(R.id.temperature_seek_bar);
        final SeekBar comfortSeekBar = findViewById(R.id.comfort_seek_bar);
        final Button ratingNextButton = findViewById(R.id.rating_next_button);

        ratingNextButton.setOnClickListener(v -> {
            UserResponse currentResponse = experiment.getCurrentUserResponse();

            currentResponse.setTemperatureFelt(temperatureSeekBar.getProgress());
            currentResponse.setComfortLevel(comfortSeekBar.getProgress());

            experiment.incrementCurrentTrial();
            Trial nextTrial = experiment.getCurrentTrial();

            Intent nextActivity;

            if (nextTrial == null) {
                nextActivity = new Intent(this, EndingActivity.class);
            } else {
                nextActivity = new Intent(this, TemperatureChangeActivity.class);
            }

            nextActivity.putExtra("experiment", experiment);
            startActivity(nextActivity);
        });
    }
}
