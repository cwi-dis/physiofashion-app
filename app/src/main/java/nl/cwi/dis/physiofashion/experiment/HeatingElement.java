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

/**
 * This class acts as an interface to the heating element given by its hostname. It provides
 * methods for heating/cooling the heating element, returning to its baseline temperature and
 * retrieving its current temperature.
 */
public class HeatingElement {
    private static final String LOG_TAG = "HeatingElement";

    /**
     * Type definition for a callback which takes a VolleyError.
     */
    @FunctionalInterface
    public interface ErrorCallback {
        void apply(VolleyError error);
    }

    /**
     * Type definition for a success callback without parameters.
     */
    @FunctionalInterface
    public interface SuccessCallback {
        void apply();
    }

    /**
     * Type definition for a success callback which takes a double value.
     */
    @FunctionalInterface
    public interface TemperatureCallback {
        void apply(double temperature);
    }

    private RequestQueue queue;
    private String hostname;
    private int baselineTemp;

    /**
     * Initialises a new instance of a heating element at the given hostname with the given baseline
     * temperature. An application context needs to be passed to setup a new request queue.
     *
     * @param context Application context, needed to create request queue
     * @param hostname Hostname of the heating element
     * @param baselineTemp Desired baseline temperature of the heating element
     */
    public HeatingElement(Context context, String hostname, int baselineTemp) {
        this.hostname = hostname;
        this.baselineTemp = baselineTemp;

        // Set up new request queue using application context
        this.queue = Volley.newRequestQueue(context);
    }

    /**
     * Returns the heating element to its baseline temperature. Invokes `onSuccess` callback if the
     * request was successful, `onError` with the HTTP error otherwise.
     *
     * @param onSuccess Callback invoked on success
     * @param onError Callback invoked on error
     */
    public void returnToBaseline(SuccessCallback onSuccess, ErrorCallback onError) {
        // Adjust setpoint to given baseline temperature
        this.adjustSetpoint(baselineTemp, onSuccess, onError);
    }

    /**
     * Heats/cools the heating element to a given target temperature. Invokes `onSuccess` callback
     * if the request was successful, `onError` with the HTTP error otherwise. The target
     * temperature is computed by adding/subtracting `tempChange` from the baseline temperature.
     * Whether to add or subtract is determined by `condition` which is usually either "heat" or
     * "cool".
     *
     * @param condition Either "heat" or "cool"
     * @param tempChange The desired temperature difference from baseline
     * @param onSuccess Callback invoked on success
     * @param onError Callback invoked on error
     */
    public void gotoTargetTemperature(String condition, int tempChange, SuccessCallback onSuccess, ErrorCallback onError) {
        // Compute target temperature from condition and temperature change
        int targetTemp = this.computeTargetTemp(condition, tempChange);
        // Adjust setpoint to calculated temperature
        this.adjustSetpoint(targetTemp, onSuccess, onError);
    }

    /**
     * Computes the effective target temperature by adding/subtracting `tempChange` from the
     * baseline temperature, based on the value of `condition` ("warm" or "cool"). If `condition`
     * has any other value, the baseline temperature is returned.
     *
     * @param condition Either "heat" or "cool"
     * @param tempChange The desired temperature difference from baseline
     * @return The calculated target temperature
     */
    private int computeTargetTemp(String condition, int tempChange) {
        int targetTemp = baselineTemp;

        // Based on the value of `condition` either add or subtract from the baseline temperature
        if (condition.compareTo("warm") == 0) {
            targetTemp += tempChange;
        } else if (condition.compareTo("cool") == 0) {
            targetTemp -= tempChange;
        }

        // Return the calculated target temperature or baseline if an invalid value for `condition`
        // was passed
        return targetTemp;
    }

    /**
     * Heats/cools the heating element to a given target temperature. Invokes `onSuccess` callback
     * if the request was successful, `onError` with the HTTP error otherwise.
     *
     * @param targetTemp The desired target temperature
     * @param onSuccess Callback invoked on success
     * @param onError Callback invoked on error
     */
    private void adjustSetpoint(int targetTemp, SuccessCallback onSuccess, ErrorCallback onError) {
        // Endpoint for adjusting the setpoint
        String url = hostname + "/api/setpoint";

        // Compile request
        StringRequest adjustRequest = new StringRequest(
                Request.Method.PUT,
                url,
                response -> onSuccess.apply(),
                onError::apply
        ) {
            @Override
            public String getBodyContentType() {
                // Set content type for request to application/json
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                // Pass desired setpoint as JSON in the request body
                return ("{ \"setpoint\": " + targetTemp + " }").getBytes();
            }
        };

        // Add request to queue
        queue.add(adjustRequest);
    }

    /**
     * Requests the current temperature from the heating element. If the temperature could be
     * requested successfully, the callback `onSuccess` is invoked with the current temperature as
     * argument, otherwise the callback `onError` is called with the specific HTTP error that
     * occurred.
     *
     * @param onSuccess Callback invoked on success with the temperature as argument
     * @param onError Callback invoked on error
     */
    public void getTemperature(TemperatureCallback onSuccess, ErrorCallback onError) {
        // Endpoint for requesting the current temperature
        String url = hostname + "/api/temperature";

        // Compile JSON request
        JsonObjectRequest tempRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Invoke success callback and pass temperature on success
                        onSuccess.apply(response.getDouble("temperature"));
                    } catch (JSONException je) {
                        // Call error callback with `null` if JSON could not be parsed
                        onError.apply(null);
                    }
                },
                onError::apply
        );

        // Add request to queue
        queue.add(tempRequest);
    }

    /**
     * Waits until the heating element reaches a given target temperature. Invokes `onSuccess`
     * callback with the last temperature reading once the temperature is reached, `onError` with a
     * HTTP error otherwise. The target temperature is computed by adding/subtracting `tempChange`
     * from the baseline temperature. Whether to add or subtract is determined by `condition` which
     * is usually either "heat", "cool" or "baseline". Moreover, a timeout in milliseconds needs to
     * be passed after which the operation times out. In that case `onSuccess` is invoked with the
     * argument -1.
     *
     * @param condition Either "heat", "cool" or "baseline"
     * @param tempChange The desired temperature difference from baseline
     * @param timeoutMs Time in milliseconds after which the operation should time out
     * @param onSuccess Callback invoked on success with the latest temperature reading, or -1 in case of timeout
     * @param onError Callback invoked on error with the HTTP error
     */
    public void onTemperatureReached(String condition, int tempChange, long timeoutMs, TemperatureCallback onSuccess, ErrorCallback onError) {
        // Calculate timestamp at which to timeout
        long timeoutAt = System.currentTimeMillis() + timeoutMs;
        // Compute target temperature from condition and temperature delta
        int targetTemp = this.computeTargetTemp(condition, tempChange);

        Timer t = new Timer();

        // Schedule timer every 100ms and start immediately
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Check whether we should time out
                if (System.currentTimeMillis() > timeoutAt) {
                    // Call success callback with -1
                    onSuccess.apply(-1);
                    // Cancel timer
                    t.cancel();
                }

                // Retrieve current temperature
                getTemperature(temp -> {
                    Log.d(LOG_TAG, "Waiting for temperature to approach " + targetTemp + ": " + temp);

                    // If condition is to warm, check if we're higher or equal to target
                    if (condition.compareTo("warm") == 0 && temp >= targetTemp) {
                        // Call success callback with last temperature reading
                        onSuccess.apply(temp);
                        // Cancel timer
                        t.cancel();
                    // If condition is to cool, check if we're lower or equal to target
                    } else if (condition.compareTo("cool") == 0 && temp <= targetTemp) {
                        // Call success callback with last temperature reading
                        onSuccess.apply(temp);
                        // Cancel timer
                        t.cancel();
                    } else if (condition.compareTo("baseline") == 0) {
                        // If we want to return to baseline, check whether the current temperature
                        // is within baseline +/- 0.2 degrees
                        if (temp >= targetTemp - 0.2 || temp <= targetTemp + 0.2) {
                            // Call success callback with last temperature reading
                            onSuccess.apply(temp);
                            // Cancel timer
                            t.cancel();
                        }
                    }
                }, error -> {
                    // Call error callback if we get a HTTP error
                    onError.apply(error);
                    // Cancel timer
                    t.cancel();
                });
            }
        }, 0, 100);
    }
}
