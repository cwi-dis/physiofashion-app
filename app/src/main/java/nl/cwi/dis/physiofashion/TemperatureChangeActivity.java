package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.Trial;

public class TemperatureChangeActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TemperatureChangeActivity";

    private Button feelItButton;
    private TextView tempChangeLabel;
    private TextView countdownLabel;

    private Experiment experiment;
    private RequestQueue queue;

    private boolean feelItButtonPressed;
    private String url;
    private Integer counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_change);

        Intent intent = this.getIntent();
        experiment = intent.getParcelableExtra("experiment");
        this.logCurrentTrial();

        url = experiment.getHostname() + "/api/setpoint";

        feelItButton = findViewById(R.id.feel_it_button);
        tempChangeLabel = findViewById(R.id.temp_change_label);
        countdownLabel = findViewById(R.id.countdown_label);

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

        queue = Volley.newRequestQueue(this);
        this.setBaselineTemperature();
    }

    private void logCurrentTrial() {
        Log.d(LOG_TAG, "Host: " + experiment.getHostname());
        Log.d(LOG_TAG, "Participant: " + experiment.getParticipantId());
        Log.d(LOG_TAG, "Counterbalance: " + experiment.getCounterBalance());
        Log.d(LOG_TAG, "Num trials: " + experiment.getTrials().size());

        Trial trial = experiment.getCurrentTrial();
        Log.d(LOG_TAG, "Trial: " + trial.isFabricOn() + " " + trial.getCondition() + " " + trial.getIntensity() + " " + trial.hasAudio());
    }

    private void setBaselineTemperature() {
        Log.d(LOG_TAG, "Setting baseline temperature: " + experiment.getBaselineTemp());

        StringRequest baselineRequest = new StringRequest(Request.Method.PUT, url, response ->
            this.pauseForAdaptation()
        , error ->
            Log.e(LOG_TAG, "Could not set adapation setpoint: " + error)
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                return ("{ \"setpoint\": " + experiment.getBaselineTemp() + " }").getBytes();
            }
        };

        queue.add(baselineRequest);
    }

    private void pauseForAdaptation() {
        Log.d(LOG_TAG, "Pausing for adaptation for " + experiment.getAdaptationPeriod() + " seconds");

        counter = experiment.getAdaptationPeriod();
        Timer t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (counter >= 0) {
                        String msg = getApplicationContext().getString(R.string.number, counter);
                        countdownLabel.setText(msg);

                        counter--;
                    }
                });
            }
        }, 0, 1000);

        new Handler().postDelayed(() -> {
            t.cancel();
            setTargetTemperature();
        }, experiment.getAdaptationPeriod() * 1000);
    }

    private void setTargetTemperature() {
        feelItButton.setEnabled(true);
        tempChangeLabel.setText(R.string.wait_stimulus);

        Trial currentTrial = experiment.getCurrentTrial();

        int tempChange = (currentTrial.getCondition().compareTo("warm") == 0) ? currentTrial.getIntensity() : -currentTrial.getIntensity();
        int targetTemp = experiment.getBaselineTemp() + tempChange;

        Log.d(LOG_TAG, "Setting target temperature to " + targetTemp);

        StringRequest baselineRequest = new StringRequest(Request.Method.PUT, url, response -> {
            experiment.getCurrentUserResponse().setStimulusStarted(
                    System.currentTimeMillis() / 1000.0
            );
            this.pauseForStimulus();
        }, error ->
            Log.e(LOG_TAG, "Could not set target setpoint: " + error)
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                return ("{ \"setpoint\": " + targetTemp + " }").getBytes();
            }
        };

        queue.add(baselineRequest);
    }

    private MediaPlayer loadAudioFile() {
        Trial currentTrial = experiment.getCurrentTrial();
        MediaPlayer mp = new MediaPlayer();

        File storage = Environment.getExternalStorageDirectory();
        File experimentDir = new File(storage, getResources().getString(R.string.app_name) + File.separator);

        try {
            mp.setDataSource(experimentDir + currentTrial.getAudioFile());
            mp.prepare();
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Could not prepare audio file: " + ioe);
        }

        return mp;
    }

    private int getAudioStartTime(String clipAlignment, int audioDuration, int stimulusDuration) {
        if (audioDuration > stimulusDuration) {
            return 0;
        }

        if (clipAlignment.compareTo("center") == 0) {
            return (int)Math.floor((stimulusDuration / 2.0) - (audioDuration / 2.0));
        } else if (clipAlignment.compareTo("end") == 0) {
            return stimulusDuration - audioDuration;
        }

        return 0;
    }

    private void pauseForStimulus() {
        Log.d(LOG_TAG, "Pausing for stimulus for " + experiment.getStimulusPeriod() + " seconds");

        counter = experiment.getStimulusPeriod();
        Timer t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (counter >= 0) {
                        String msg = getApplicationContext().getString(R.string.number, counter);
                        countdownLabel.setText(msg);

                        counter--;
                    }
                });
            }
        }, 0, 1000);

        new Handler().postDelayed(() -> {
            Log.d(LOG_TAG, "Stimulus wait period passed");
            t.cancel();

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
        }, experiment.getStimulusPeriod() * 1000);
    }

    private void launchRatingActivity() {
        StringRequest baselineRequest = new StringRequest(Request.Method.PUT, url, response ->
            Log.d(LOG_TAG, "Returned to baseline temperature")
        , error ->
            Log.e(LOG_TAG, "Could not return to baseline temperature: " + error)
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                return ("{ \"setpoint\": " + experiment.getBaselineTemp() + " }").getBytes();
            }
        };

        queue.add(baselineRequest);

        Intent ratingIntent = new Intent(this, RatingActivity.class);
        ratingIntent.putExtra("experiment", experiment);

        startActivity(ratingIntent);
    }

    @Override
    public void onBackPressed() {
    }
}
