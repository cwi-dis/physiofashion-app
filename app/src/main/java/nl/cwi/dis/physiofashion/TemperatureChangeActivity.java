package nl.cwi.dis.physiofashion;

import android.content.Intent;
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

import java.util.Timer;
import java.util.TimerTask;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.Trial;

public class TemperatureChangeActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TemperatureChangeActivity";

    private static final int BASELINE_TEMP = 32;
    private static final int STIMULUS_PAUSE = 10;
    private static final int ADAPTATION_PAUSE = 20;

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

        feelItButtonPressed = false;
        feelItButton.setOnClickListener(v -> {
            tempChangeLabel.setText(R.string.pressed_stimulus);
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
        Log.d(LOG_TAG, "Setting baseline temperature");

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
                return ("{ \"setpoint\": " + BASELINE_TEMP + " }").getBytes();
            }
        };

        queue.add(baselineRequest);
    }

    private void pauseForAdaptation() {
        Log.d(LOG_TAG, "Pausing for adaptation for " + ADAPTATION_PAUSE + " seconds");

        counter = ADAPTATION_PAUSE;
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
        }, ADAPTATION_PAUSE * 1000);
    }

    private void setTargetTemperature() {
        feelItButton.setEnabled(true);
        tempChangeLabel.setText(R.string.wait_stimulus);

        Trial currentTrial = experiment.getCurrentTrial();

        int tempChange = (currentTrial.getCondition().compareTo("warm") == 0) ? currentTrial.getIntensity() : -currentTrial.getIntensity();
        int targetTemp = BASELINE_TEMP + tempChange;

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

    private void pauseForStimulus() {
        Log.d(LOG_TAG, "Pausing for stimulus for " + STIMULUS_PAUSE + " seconds");

        counter = STIMULUS_PAUSE;
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
        }, STIMULUS_PAUSE * 1000);
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
                return ("{ \"setpoint\": " + BASELINE_TEMP + " }").getBytes();
            }
        };

        queue.add(baselineRequest);

        Intent ratingIntent = new Intent(this, RatingActivity.class);
        ratingIntent.putExtra("experiment", experiment);

        startActivity(ratingIntent);
    }
}
