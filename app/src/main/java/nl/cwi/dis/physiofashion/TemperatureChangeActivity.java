package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.HeatingElement;
import nl.cwi.dis.physiofashion.experiment.Trial;

/**
 * This activity is responsible for running the current trial as specified in the experiment
 * configuration. The configuration dictates the control of the heating element, whether an audio
 * file should be played during the trial and logs all user input. At the end, it will redirect
 * the user to an activity where they will rate their experience.
 */
public class TemperatureChangeActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TemperatureChangeActivity";

    private Button feelItButton;
    private TextView tempChangeLabel;
    private TextView countdownLabel;

    private Experiment experiment;
    private HeatingElement heatingElement;
    private MediaPlayer audioPlayer;

    private boolean feelItButtonPressed;

    /**
     * Sets up the UI and installs events handlers for UI elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_change);

        // Retrieve experiment data from intent and log current trial to the system log
        Intent intent = this.getIntent();
        experiment = intent.getParcelableExtra("experiment");
        this.logCurrentTrial();

        feelItButton = findViewById(R.id.feel_it_button);
        tempChangeLabel = findViewById(R.id.temp_change_label);
        countdownLabel = findViewById(R.id.countdown_label);

        // Hide 'I feel it' button is the trial has an audio file associated to it
        if (experiment.getCurrentTrial().hasAudio()) {
            feelItButton.setVisibility(View.INVISIBLE);
        }

        // Generate string for trial counter
        String msg = getApplicationContext().getString(
                R.string.trial_counter,
                experiment.getCurrentTrialIndex() + 1,
                experiment.getTrials().size()
        );

        // Set trial counter on the UI
        final TextView trialCounter = findViewById(R.id.trial_counter);
        trialCounter.setText(msg);

        // Install handler for 'I feel it' button
        feelItButtonPressed = false;
        feelItButton.setOnClickListener(v -> {
            tempChangeLabel.setText(R.string.pressed_stimulus);
            feelItButton.setEnabled(false);
            feelItButtonPressed = true;

            // Store timestamp when the button was pressed
            experiment.getCurrentUserResponse().setStimulusFelt(
                    System.currentTimeMillis() / 1000.0
            );
        });

        // Initialise heating element and set baseline temperature
        heatingElement = new HeatingElement(this, experiment.getHostname(), experiment.getBaselineTemp());
        this.setBaselineTemperature();
    }

    /**
     * This method logs the current trial to the system log
     */
    private void logCurrentTrial() {
        Log.d(LOG_TAG, "Host: " + experiment.getHostname());
        Log.d(LOG_TAG, "Participant: " + experiment.getParticipantId());
        Log.d(LOG_TAG, "Counterbalance: " + experiment.getCounterBalance());
        Log.d(LOG_TAG, "Num trials: " + experiment.getTrials().size());

        Trial trial = experiment.getCurrentTrial();
        Log.d(LOG_TAG, "Trial: " + trial.getExternalCondition() + " " + trial.getCondition() + " " + trial.getIntensity() + " " + trial.getAudioFile());
    }

    /**
     * Set the heating element to the configured baseline temperature
     */
    private void setBaselineTemperature() {
        Log.d(LOG_TAG, "Setting baseline temperature: " + experiment.getBaselineTemp());

        // Return to baseline and then call method `pauseForAdaptation()`
        heatingElement.returnToBaseline(
            this::pauseForAdaptation
        , (error) ->
            Log.e(LOG_TAG, "Could not set adaptation setpoint: " + error)
        );
    }

    /**
     * Pauses the app for the user to adapt to the current temperature for the amount of time
     * configured in the experiment config
     */
    private void pauseForAdaptation() {
        Log.d(LOG_TAG, "Pausing for adaptation for " + experiment.getAdaptationPeriod() + " seconds");

        // Create countdown timer and set it to the time given in the experiment config
        CountDownTimer countdown = new CountDownTimer(experiment.getAdaptationPeriod() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update countdown label on each tick
                String msg = getApplicationContext().getString(R.string.number, millisUntilFinished / 1000);
                countdownLabel.setText(msg);
            }

            @Override
            public void onFinish() {
                // Set countdown label to zero after countdown has expired
                countdownLabel.setText(R.string.zero);
            }
        }.start();

        // Create new handler and call `setTargetTemperature` after the adaptation time given in the
        // experiment config
        new Handler().postDelayed(() -> {
            countdown.cancel();
            setTargetTemperature();
        }, experiment.getAdaptationPeriod() * 1000);
    }

    /**
     * Sets the heating element to the target temperature specified in the experiment config for the
     * current trial. If the experiment file specifies a stimulus period, continue after the period
     * has elapsed, otherwise continue after the target temperature has been reached.
     */
    private void setTargetTemperature() {
        // Get current trial
        Trial currentTrial = experiment.getCurrentTrial();

        // Go to target temperature. The target is given by a condition (`heat` or `cool`) and an
        // intensity (a temperature delta). The method invokes a callback once the request has
        // returned successfully.
        heatingElement.gotoTargetTemperature(
                currentTrial.getCondition(),
                currentTrial.getIntensity(),
                () -> {
                    // Log time when stimulus was started
                    experiment.getCurrentUserResponse().setStimulusStarted(
                            System.currentTimeMillis() / 1000.0
                    );

                    // If the stimulus period is 0 call method to pause until temperature has been
                    // reached, otherwise call method which waits until stimulus period has passed.
                    if (experiment.getStimulusPeriod() == 0) {
                        this.pauseUntilTargetReached();
                    } else {
                        this.pauseForStimulus();
                    }
                },
                (error) -> Log.e(LOG_TAG, "Could not set target setpoint: " + error)
        );
    }

    /**
     * Instructs the app to pause until a given target temperature has been reached. This method
     * should only be called in conjunction with a call to `gotoTargetTemperature()`.
     */
    private void pauseUntilTargetReached() {
        // Update the UI label informing the user to wait
        tempChangeLabel.setText(R.string.wait_temperature);

        // Get current trial
        Trial currentTrial = experiment.getCurrentTrial();

        // Wait for the temperature to `heat`/`cool` with the given delta and a timeout of 10s
        heatingElement.onTemperatureReached(currentTrial.getCondition(), currentTrial.getIntensity(), 10000, temp -> {
            // Temperature was reached, start audio file
            Log.d(LOG_TAG, "Target temperature reached, playing audio file");
            tempChangeLabel.setText(R.string.playing_audio);
            feelItButton.setEnabled(true);

            // Load audio file and play it
            loadAudioFile();
            audioPlayer.start();

            // Stop the player and release it after the clip has finished
            audioPlayer.setOnCompletionListener(mp -> {
                Log.d(LOG_TAG, "Clip finished playing");

                audioPlayer.stop();
                audioPlayer.release();

                // Wait for user interaction
                waitForButtonPress();
            });
        }, error -> {});
    }

    /**
     * Loads the audio file for the current trial and prepares it.
     */
    private void loadAudioFile() {
        // Get current trial and init audio player object
        Trial currentTrial = experiment.getCurrentTrial();
        audioPlayer = new MediaPlayer();

        // Load audio file from storage
        File experimentDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
        File audioPath = new File(experimentDir, currentTrial.getAudioFile());

        // Set up player
        try {
            Log.d(LOG_TAG, "Loading audio file from: " + audioPath.getAbsolutePath());
            audioPlayer.setDataSource(audioPath.getAbsolutePath());
            audioPlayer.prepare();
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Could not prepare audio file: " + ioe);
        }
    }

    /**
     * Returns the start time for an audio clip with the given length based on clip alignment,
     * stimulus length and alignment correction given in the experiment config.
     *
     * @param audioDuration Duration of the audio clip we want to play
     * @return The calculated clip start time, factoring in experiment config
     */
    private int getAudioStartTime(int audioDuration) {
        int startTime = 0;

        // Get clip alignment, stimulus length and clip alignment from experiment config
        String clipAlignment = experiment.getClipAlignment();
        int stimulusDuration = experiment.getStimulusPeriod() * 1000;
        int alignmentCorrection = (int)Math.round(experiment.getAlignmentCorrection() * 1000);

        // Calculate start time based on clip length and experiment config
        if (audioDuration > stimulusDuration) {
            startTime = alignmentCorrection;
        } else if (clipAlignment.compareTo("center") == 0) {
            startTime = (int)Math.floor((stimulusDuration / 2.0) - (audioDuration / 2.0)) + alignmentCorrection;
        } else if (clipAlignment.compareTo("end") == 0) {
            startTime =  stimulusDuration - audioDuration + alignmentCorrection;
        }

        // Make sure start time is not negative and return the calculated start time
        return (startTime < 0) ? 0 : startTime;
    }

    /**
     * Pauses the app for the time given by the experiment file and plays the audio clip associated
     * with the current trial if available.
     */
    private void pauseForStimulus() {
        Trial currentTrial = experiment.getCurrentTrial();
        feelItButton.setEnabled(true);

        // Adjust the label depending on whether the current trial has audio
        if (!currentTrial.hasAudio()) {
            tempChangeLabel.setText(R.string.wait_stimulus);
        } else {
            tempChangeLabel.setText(R.string.playing_audio);
        }

        Log.d(LOG_TAG, "Pausing for stimulus for " + experiment.getStimulusPeriod() + " seconds");

        // Load audio file and start playing it after the calculated time if trial has audio
        if (currentTrial.hasAudio()) {
            this.loadAudioFile();
            int startTimeMs = this.getAudioStartTime(audioPlayer.getDuration());

            Log.d(LOG_TAG, "Starting audio playback after " + startTimeMs + "ms");
            new Handler().postDelayed(audioPlayer::start, startTimeMs);
        }

        // Show countdown during the stimulus period
        CountDownTimer countdown = new CountDownTimer(experiment.getStimulusPeriod() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update countdown label after each tick
                String msg = getApplicationContext().getString(R.string.number, millisUntilFinished / 1000);
                countdownLabel.setText(msg);
            }

            @Override
            public void onFinish() {
                // Set countdown to zero after countdown has expired
                countdownLabel.setText(R.string.zero);
            }
        }.start();

        // Stop and release audio player and call method to wait for user input after stimulus
        // period has elapsed
        new Handler().postDelayed(() -> {
            Log.d(LOG_TAG, "Stimulus wait period passed");
            countdown.cancel();

            // Stop and release audio player if current trial had audio
            if (audioPlayer != null) {
                audioPlayer.stop();
                audioPlayer.release();
            }

            // Wait for user interaction
            waitForButtonPress();
        }, experiment.getStimulusPeriod() * 1000);
    }

    /**
     * Checks whether the user has pressed the 'I feel it button' already and launches the rating
     * activity if so. Otherwise it keeps waiting for the user to press the button. Moreover, if
     * the current trial has had an audio file associated to it, the next activity is launched
     * directly.
     */
    private void waitForButtonPress() {
        // IF the current trial has an audio file, launch the next activity directly
        if (experiment.getCurrentTrial().hasAudio()) {
            experiment.getCurrentUserResponse().setStimulusFelt(
                    System.currentTimeMillis() / 1000.0
            );
            launchRatingActivity();
        }

        // Check whether the button was pressed before
        if (feelItButtonPressed) {
            launchRatingActivity();
        } else {
            // Install new handler that waits for the button press and logs the timestamp
            feelItButton.setOnClickListener(v -> {
                experiment.getCurrentUserResponse().setStimulusFelt(
                        System.currentTimeMillis() / 1000.0
                );
                launchRatingActivity();
            });
        }
    }

    /**
     * This method resets the heating element and prepares the data for the next activity where the
     * user will rate their experience during the trial.
     */
    private void launchRatingActivity() {
        // Return the heating element to baseline temperature
        heatingElement.returnToBaseline(() ->
            Log.d(LOG_TAG, "Returned to baseline temperature")
        , error ->
            Log.e(LOG_TAG, "Could not return to baseline temperature: " + error)
        );

        Intent ratingIntent;

        // Determine whether the next activity should use Likert scales or self-assessment Manikins
        if (experiment.getQuestionType().compareTo("likert") == 0) {
            ratingIntent = new Intent(this, RatingActivity.class);
        } else {
            ratingIntent = new Intent(this, ManikinRatingActivity.class);
        }

        // Pass experiment config to next activity and launch it
        ratingIntent.putExtra("experiment", experiment);
        startActivity(ratingIntent);
    }

    /**
     * Do nothing if the user presses the back button
     */
    @Override
    public void onBackPressed() {
    }
}
