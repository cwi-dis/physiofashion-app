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

public class TemperatureChangeActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TemperatureChangeActivity";

    private Button feelItButton;
    private TextView tempChangeLabel;
    private TextView countdownLabel;

    private Experiment experiment;
    private HeatingElement heatingElement;
    private MediaPlayer audioPlayer;

    private boolean feelItButtonPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_change);

        Intent intent = this.getIntent();
        experiment = intent.getParcelableExtra("experiment");
        this.logCurrentTrial();

        feelItButton = findViewById(R.id.feel_it_button);
        tempChangeLabel = findViewById(R.id.temp_change_label);
        countdownLabel = findViewById(R.id.countdown_label);

        if (experiment.getCurrentTrial().hasAudio()) {
            feelItButton.setVisibility(View.INVISIBLE);
        }

        String msg = getApplicationContext().getString(
                R.string.trial_counter,
                experiment.getCurrentTrialIndex() + 1,
                experiment.getTrials().size()
        );

        final TextView trialCounter = findViewById(R.id.trial_counter);
        trialCounter.setText(msg);

        feelItButtonPressed = false;
        feelItButton.setOnClickListener(v -> {
            tempChangeLabel.setText(R.string.pressed_stimulus);
            feelItButton.setEnabled(false);
            feelItButtonPressed = true;

            experiment.getCurrentUserResponse().setStimulusFelt(
                    System.currentTimeMillis() / 1000.0
            );
        });

        heatingElement = new HeatingElement(this, experiment.getHostname(), experiment.getBaselineTemp());
        this.setBaselineTemperature();
    }

    private void logCurrentTrial() {
        Log.d(LOG_TAG, "Host: " + experiment.getHostname());
        Log.d(LOG_TAG, "Participant: " + experiment.getParticipantId());
        Log.d(LOG_TAG, "Counterbalance: " + experiment.getCounterBalance());
        Log.d(LOG_TAG, "Num trials: " + experiment.getTrials().size());

        Trial trial = experiment.getCurrentTrial();
        Log.d(LOG_TAG, "Trial: " + trial.getExternalCondition() + " " + trial.getCondition() + " " + trial.getIntensity() + " " + trial.getAudioFile());
    }

    private void setBaselineTemperature() {
        Log.d(LOG_TAG, "Setting baseline temperature: " + experiment.getBaselineTemp());

        heatingElement.returnToBaseline(
            this::pauseForAdaptation
        , (error) ->
            Log.e(LOG_TAG, "Could not set adapation setpoint: " + error)
        );
    }

    private void pauseForAdaptation() {
        Log.d(LOG_TAG, "Pausing for adaptation for " + experiment.getAdaptationPeriod() + " seconds");

        CountDownTimer countdown = new CountDownTimer(experiment.getAdaptationPeriod() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String msg = getApplicationContext().getString(R.string.number, millisUntilFinished / 1000);
                countdownLabel.setText(msg);
            }

            @Override
            public void onFinish() {
                countdownLabel.setText(R.string.zero);
            }
        }.start();

        new Handler().postDelayed(() -> {
            countdown.cancel();
            setTargetTemperature();
        }, experiment.getAdaptationPeriod() * 1000);
    }

    private void setTargetTemperature() {
        Trial currentTrial = experiment.getCurrentTrial();

        heatingElement.gotoTargetTemperature(
                currentTrial.getCondition(),
                currentTrial.getIntensity(),
                () -> {
                    experiment.getCurrentUserResponse().setStimulusStarted(
                            System.currentTimeMillis() / 1000.0
                    );

                    if (experiment.getStimulusPeriod() == 0) {
                        this.pauseUntilTargetReached();
                    } else {
                        this.pauseForStimulus();
                    }
                },
                (error) -> Log.e(LOG_TAG, "Could not set target setpoint: " + error)
        );
    }

    private void pauseUntilTargetReached() {
        tempChangeLabel.setText(R.string.wait_temperature);

        Trial currentTrial = experiment.getCurrentTrial();

        heatingElement.onTemperatureReached(currentTrial.getCondition(), currentTrial.getIntensity(), 10000, temp -> {
            Log.d(LOG_TAG, "Target temperature reached, playing audio file");
            tempChangeLabel.setText(R.string.playing_audio);
            feelItButton.setEnabled(true);

            loadAudioFile();
            audioPlayer.start();

            audioPlayer.setOnCompletionListener(mp -> {
                Log.d(LOG_TAG, "Clip finished playing");

                audioPlayer.stop();
                audioPlayer.release();

                waitForButtonPress();
            });
        }, error -> {});
    }

    private void loadAudioFile() {
        Trial currentTrial = experiment.getCurrentTrial();
        audioPlayer = new MediaPlayer();

        File experimentDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
        File audioPath = new File(experimentDir, currentTrial.getAudioFile());

        try {
            Log.d(LOG_TAG, "Loading audio file from: " + audioPath.getAbsolutePath());
            audioPlayer.setDataSource(audioPath.getAbsolutePath());
            audioPlayer.prepare();
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Could not prepare audio file: " + ioe);
        }
    }

    private int getAudioStartTime(int audioDuration) {
        int startTime = 0;

        String clipAlignment = experiment.getClipAlignment();
        int stimulusDuration = experiment.getStimulusPeriod() * 1000;
        int alignmentCorrection = (int)Math.round(experiment.getAlignmentCorrection() * 1000);

        if (audioDuration > stimulusDuration) {
            startTime = alignmentCorrection;
        } else if (clipAlignment.compareTo("center") == 0) {
            startTime = (int)Math.floor((stimulusDuration / 2.0) - (audioDuration / 2.0)) + alignmentCorrection;
        } else if (clipAlignment.compareTo("end") == 0) {
            startTime =  stimulusDuration - audioDuration + alignmentCorrection;
        }

        return (startTime < 0) ? 0 : startTime;
    }

    private void pauseForStimulus() {
        Trial currentTrial = experiment.getCurrentTrial();
        feelItButton.setEnabled(true);

        if (!currentTrial.hasAudio()) {
            tempChangeLabel.setText(R.string.wait_stimulus);
        } else {
            tempChangeLabel.setText(R.string.playing_audio);
        }

        Log.d(LOG_TAG, "Pausing for stimulus for " + experiment.getStimulusPeriod() + " seconds");

        if (currentTrial.hasAudio()) {
            this.loadAudioFile();
            int startTimeMs = this.getAudioStartTime(audioPlayer.getDuration());

            Log.d(LOG_TAG, "Starting audio playback after " + startTimeMs + "ms");
            new Handler().postDelayed(audioPlayer::start, startTimeMs);
        }

        CountDownTimer countdown = new CountDownTimer(experiment.getStimulusPeriod() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String msg = getApplicationContext().getString(R.string.number, millisUntilFinished / 1000);
                countdownLabel.setText(msg);
            }

            @Override
            public void onFinish() {
                countdownLabel.setText(R.string.zero);
            }
        }.start();

        new Handler().postDelayed(() -> {
            Log.d(LOG_TAG, "Stimulus wait period passed");
            countdown.cancel();

            if (audioPlayer != null) {
                audioPlayer.stop();
                audioPlayer.release();
            }

            waitForButtonPress();
        }, experiment.getStimulusPeriod() * 1000);
    }

    private void waitForButtonPress() {
        if (experiment.getCurrentTrial().hasAudio()) {
            experiment.getCurrentUserResponse().setStimulusFelt(
                    System.currentTimeMillis() / 1000.0
            );
            launchRatingActivity();
        }

        if (feelItButtonPressed) {
            launchRatingActivity();
        } else {
            feelItButton.setOnClickListener(v -> {
                experiment.getCurrentUserResponse().setStimulusFelt(
                        System.currentTimeMillis() / 1000.0
                );
                launchRatingActivity();
            });
        }
    }

    private void launchRatingActivity() {
        heatingElement.returnToBaseline(() ->
            Log.d(LOG_TAG, "Returned to baseline temperature")
        , error ->
            Log.e(LOG_TAG, "Could not return to baseline temperature: " + error)
        );

        Intent ratingIntent;

        if (experiment.getQuestionType().compareTo("likert") == 0) {
            ratingIntent = new Intent(this, RatingActivity.class);
        } else {
            ratingIntent = new Intent(this, ManikinRatingActivity.class);
        }

        ratingIntent.putExtra("experiment", experiment);
        startActivity(ratingIntent);
    }

    @Override
    public void onBackPressed() {
    }
}
