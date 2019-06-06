package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;

import nl.cwi.dis.physiofashion.experiment.Experiment;
import nl.cwi.dis.physiofashion.experiment.UserResponse;

public class EndingActivity extends AppCompatActivity {
    public static final String LOG_TAG = "EndingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ending);

        Intent intent = this.getIntent();
        Experiment experiment = intent.getParcelableExtra("experiment");
        ArrayList<UserResponse> responses = experiment.getResponses();

        for (UserResponse response : responses) {
            Log.d(LOG_TAG, "started: " + response.getStimulusStarted() + " felt: " + response.getStimulusFelt() + " feeling: " + response.getTemperatureFelt() + " comfort: " + response.getComfortLevel());
        }

        final Button returnToStart = findViewById(R.id.return_to_start);
        returnToStart.setOnClickListener(v -> {
            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
        });
    }

    @Override
    public void onBackPressed() {
    }
}
