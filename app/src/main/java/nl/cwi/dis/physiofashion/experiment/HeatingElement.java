package nl.cwi.dis.physiofashion.experiment;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

public class HeatingElement {
    private static final String LOG_TAG = "HeatingElement";

    @FunctionalInterface
    public interface ErrorCallback {
        void apply(VolleyError error);
    }

    @FunctionalInterface
    public interface SetpointCallback {
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

    public void returnToBaseline(SetpointCallback onSuccess, ErrorCallback onError) {
        this.adjustSetpoint(baselineTemp, onSuccess, onError);
    }

    public void gotoTargetTemperature(String condition, int tempChange, SetpointCallback onSuccess, ErrorCallback onError) {
        int targetTemp = baselineTemp;

        if (condition.compareTo("warm") == 0) {
            targetTemp += tempChange;
        } else if (condition.compareTo("cool") == 0) {
            targetTemp -= tempChange;
        }

        this.adjustSetpoint(targetTemp, onSuccess, onError);
    }

    private void adjustSetpoint(int targetTemp, SetpointCallback onSuccess, ErrorCallback onError) {
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
}
