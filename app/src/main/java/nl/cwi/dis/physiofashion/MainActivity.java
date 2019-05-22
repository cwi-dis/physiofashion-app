package nl.cwi.dis.physiofashion;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView label = findViewById(R.id.label);

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Log.e(TAG, "No drivers found");
            label.setText("No drivers found");
            return;
        }

        String drivers = availableDrivers.stream().map((driver) -> driver.getDevice().getDeviceName()).reduce("", (acc, name) -> acc + " " + name);
        label.setText("Available drivers: " + drivers);

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            label.setText("Could not establish connection");
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

        Thread writeThread = new Thread(() -> {
            UsbSerialPort port = driver.getPorts().get(0);

            try {
                port.open(connection);
                port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                port.setDTR(true);
                port.setRTS(true);

                while (true) {
                    try {
                        byte[] data = {'a'};
                        int bytesWritten = port.write(data, 10000);

                        Log.d(TAG, "Bytes written " + bytesWritten);
                    } catch (IOException e) {
                        Log.e(TAG, "Write exception:" + e);
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread interrupted: " + e);
                        break;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Write exception: " + e);
            }

            try {
                port.close();
            } catch (IOException e) {}
        });

        writeThread.start();
    }
}
