package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

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
            String formattedResponse = String.format(
                    Locale.ENGLISH,
                    "%.2f, %.2f, %d, %d",
                    response.getStimulusStarted(),
                    response.getStimulusFelt(),
                    response.getTemperatureFelt(),
                    response.getComfortLevel()
            );

            Log.d(LOG_TAG, formattedResponse);
        }

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        experiment.writeResponsesToFile(downloadDir);

        final Button returnToStart = findViewById(R.id.return_to_start);
        returnToStart.setOnClickListener(v -> {
            Intent mainActivity = new Intent(this, MainActivity.class);
            mainActivity.putExtra("participant", experiment.getParticipantId());

            startActivity(mainActivity);
        });
    }

    @Override
    public void onBackPressed() {
    }
}
