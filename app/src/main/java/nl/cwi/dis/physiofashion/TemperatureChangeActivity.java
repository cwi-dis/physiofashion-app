package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.Trial;

public class TemperatureChangeActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TemperatureChangeActivity";
    private static final int BASELINE_TEMP = 32;

    private Experiment experiment;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_change);

        Intent intent = this.getIntent();
        experiment = intent.getParcelableExtra("experiment");
        this.logCurrentTrial();

        final Button feelItButton = findViewById(R.id.feel_it);
        feelItButton.setOnClickListener((View v) -> {
            Intent ratingIntent = new Intent(TemperatureChangeActivity.this, RatingActivity.class);
            ratingIntent.putExtra("experiment", experiment);

            startActivity(ratingIntent);
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

        String url = experiment.getHostname() + "/api/setpoint";
        StringRequest baselineRequest = new StringRequest(Request.Method.PUT, url, response -> {
            this.pauseForAdaptation(20);
        }, error -> {
            Log.e(LOG_TAG, "Could not set adapation setpoint: " + error);
        }) {
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

    private void pauseForAdaptation(int timeInSeconds) {
        Log.d(LOG_TAG, "Pausing for adaptation for " + timeInSeconds + " seconds");
        new Handler().postDelayed(this::setTargetTemperature, timeInSeconds * 1000);
    }

    private void setTargetTemperature() {
        Trial currentTrial = experiment.getCurrentTrial();

        int tempChange = (currentTrial.getCondition().compareTo("warm") == 0) ? currentTrial.getIntensity() : -currentTrial.getIntensity();
        int targetTemp = BASELINE_TEMP + tempChange;

        Log.d(LOG_TAG, "Setting target temperature to " + targetTemp);

        String url = experiment.getHostname() + "/api/setpoint";
        StringRequest baselineRequest = new StringRequest(Request.Method.PUT, url, response -> {
            this.pauseForStimulus(10);
        }, error -> {
            Log.e(LOG_TAG, "Could not set target setpoint: " + error);
        }) {
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

    private void pauseForStimulus(int timeInSeconds) {
        Log.d(LOG_TAG, "Pausing for stimulus for " + timeInSeconds + " seconds");
    }
}
