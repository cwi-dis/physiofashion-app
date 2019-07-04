package nl.cwi.dis.physiofashion.experiment;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class HeatingElement {
    private static final String LOG_TAG = "HeatingElement";

    @FunctionalInterface
    public interface ErrorCallback {
        void apply(VolleyError error);
    }

    @FunctionalInterface
    public interface SuccessCallback {
        void apply();
    }

    @FunctionalInterface
    public interface TemperatureCallback {
        void apply(double temperature);
    }

    private RequestQueue queue;
    private String hostname;
    private int baselineTemp;

    public HeatingElement(Context context, String hostname, int baselineTemp) {
        this.hostname = hostname;
        this.baselineTemp = baselineTemp;

        this.queue = Volley.newRequestQueue(context);
    }

    public void returnToBaseline(SuccessCallback onSuccess, ErrorCallback onError) {
        this.adjustSetpoint(baselineTemp, onSuccess, onError);
    }

    public void gotoTargetTemperature(String condition, int tempChange, SuccessCallback onSuccess, ErrorCallback onError) {
        int targetTemp = this.computeTargetTemp(condition, tempChange);
        this.adjustSetpoint(targetTemp, onSuccess, onError);
    }

    private int computeTargetTemp(String condition, int tempChange) {
        int targetTemp = baselineTemp;

        if (condition.compareTo("warm") == 0) {
            targetTemp += tempChange;
        } else if (condition.compareTo("cool") == 0) {
            targetTemp -= tempChange;
        }

        return targetTemp;
    }

    private void adjustSetpoint(int targetTemp, SuccessCallback onSuccess, ErrorCallback onError) {
        String url = hostname + "/api/setpoint";

        StringRequest adjustRequest = new StringRequest(
                Request.Method.PUT,
                url,
                response -> onSuccess.apply(),
                onError::apply
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

        queue.add(adjustRequest);
    }

    public void getTemperature(TemperatureCallback onSuccess, ErrorCallback onError) {
        String url = hostname + "/api/temperature";

        JsonObjectRequest tempRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        onSuccess.apply(response.getDouble("temperature"));
                    } catch (JSONException je) {
                        onError.apply(null);
                    }
                },
                onError::apply
        );

        queue.add(tempRequest);
    }

    public void onTemperatureReached(String condition, int tempChange, long timeoutMs, TemperatureCallback onSuccess, ErrorCallback onError) {
        long timeoutAt = System.currentTimeMillis() + timeoutMs;
        int targetTemp = this.computeTargetTemp(condition, tempChange);

        Timer t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() > timeoutAt) {
                    onSuccess.apply(-1);
                    t.cancel();
                }

                getTemperature(temp -> {
                    Log.d(LOG_TAG, "Waiting for temperature to approach " + targetTemp + ": " + temp);

                    if (condition.compareTo("warm") == 0 && temp >= targetTemp) {
                        onSuccess.apply(temp);
                        t.cancel();
                    } else if (condition.compareTo("cool") == 0 && temp <= targetTemp) {
                        onSuccess.apply(temp);
                        t.cancel();
                    } else if (condition.compareTo("baseline") == 0) {
                        if (temp >= targetTemp - 0.2 || temp <= targetTemp + 0.2) {
                            onSuccess.apply(temp);
                            t.cancel();
                        }
                    }
                }, error -> {
                    onError.apply(error);
                    t.cancel();
                });
            }
        }, 0, 100);
    }
}
