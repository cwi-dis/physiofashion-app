package nl.cwi.dis.physiofashion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView label = findViewById(R.id.label);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                    STORAGE_PERMISSION_REQUEST
            );
        } else {
            Log.d(LOG_TAG, "Permission already granted");
            this.setupUI();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.setupUI();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("File system permissions")
                        .setMessage("The app needs access to external storage in order to function properly. Please restart the app and grant the permission.")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            finishAffinity();
                            System.exit(0);
                        })
                        .show();
            }
        }
    }

    private void setupUI() {

    }
}
