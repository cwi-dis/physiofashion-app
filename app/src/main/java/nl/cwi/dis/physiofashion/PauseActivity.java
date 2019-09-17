package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import nl.cwi.dis.physiofashion.experiment.Experiment;

/**
 * This activity is used to give the user a pause in between trials. This pause comes in two forms,
 * one where the user can pause as long as they want, e.g. for changing the external condition and
 * one which imposes a minimum pause duration by means of a countdown. The 'Continue' button is
 * only enabled after the countdown has elapsed. The duration of the countdown is specifiec in the
 * experiment configuration.
 */
public class PauseActivity extends AppCompatActivity {
    /**
     * Sets up the UI and installs events handlers for UI elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pause);

        Intent intent = this.getIntent();

        // Get experiment config and whether the activity should show a countdown from the intent
        boolean noCountdown = intent.getBooleanExtra("noCountdown", false);
        Experiment experiment = intent.getParcelableExtra("experiment");

        final TextView countdownLabel = findViewById(R.id.countdown_label);
        final TextView waitLabel = findViewById(R.id.wait_message);
        final Button continueButton = findViewById(R.id.continue_button);

        // If `noCountdown` is true or the break duration specified in the experiment config is
        // equal to zero, enable the 'Continue' button immediately, otherwise initialise countdown
        if (noCountdown || experiment.getBreakDuration() == 0) {
            continueButton.setEnabled(true);
            waitLabel.setText(R.string.pause);
        } else {
            // Disable 'Continue' button initially
            continueButton.setEnabled(false);

            // Initialise new countdown and set it to the break duration found in the experiment
            // config
            new CountDownTimer(experiment.getBreakDuration() * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Update label on each tick
                    String msg = getApplicationContext().getString(R.string.number, millisUntilFinished / 1000);
                    countdownLabel.setText(msg);
                }

                @Override
                public void onFinish() {
                    // Enable 'Continue' button after countdown has elapsed
                    continueButton.setEnabled(true);
                    countdownLabel.setText(R.string.zero);
                }
            }.start();
        }

        // Install click handler for 'Continue' button
        continueButton.setOnClickListener(v -> {
            // Initialise intent for launching TemperatureChangeActivity and pass experiment config
            Intent nextActivity = new Intent(this, TemperatureChangeActivity.class);
            nextActivity.putExtra("experiment", experiment);

            startActivity(nextActivity);
        });
    }
}
