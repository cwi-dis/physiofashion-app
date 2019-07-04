package nl.cwi.dis.physiofashion;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import nl.cwi.dis.physiofashion.experiment.Experiment;

public class PauseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pause);

        Intent intent = this.getIntent();

        boolean noCountdown = intent.getBooleanExtra("noCountdown", false);
        Experiment experiment = intent.getParcelableExtra("experiment");

        final TextView countdownLabel = findViewById(R.id.countdown_label);
        final TextView waitLabel = findViewById(R.id.wait_message);
        final Button continueButton = findViewById(R.id.continue_button);

        if (noCountdown) {
            continueButton.setEnabled(true);
            waitLabel.setText(R.string.pause);
        } else {
            continueButton.setEnabled(false);

            new CountDownTimer(experiment.getBreakDuration() * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String msg = getApplicationContext().getString(R.string.number, millisUntilFinished / 1000);
                    countdownLabel.setText(msg);
                }

                @Override
                public void onFinish() {
                    continueButton.setEnabled(true);
                    countdownLabel.setText(R.string.zero);
                }
            }.start();
        }

        continueButton.setOnClickListener(v -> {
            Intent nextActivity = new Intent(this, TemperatureChangeActivity.class);
            nextActivity.putExtra("experiment", experiment);

            startActivity(nextActivity);
        });
    }
}
