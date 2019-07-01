package nl.cwi.dis.physiofashion.experiment;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class ExperimentParser {
    private static final String LOG_TAG = "ExperimentParser";
    private JSONObject experiment;

    public ExperimentParser(File experimentFile) {
        this.experiment = this.readExperimentFromFile(experimentFile);
    }

    private JSONObject readExperimentFromFile(File experimentFile) {
        try {
            Scanner scanner = new Scanner(experimentFile);
            String fileContents = scanner.useDelimiter("\\A").next();
            scanner.close();

            return new JSONObject(fileContents);
        } catch (FileNotFoundException fnf) {
            Log.e(LOG_TAG, "Experiment file not found: " + fnf);
            return null;
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Could not parse JSON object: " + je);
            return null;
        }
    }

    public boolean isValidExperiment() {
        return experiment != null;
    }

    public String getHostname() {
        return experiment.optString("hostname", null);
    }

    public int getBaselineTemperature() {
        return experiment.optInt("baselineTemperature", 32);
    }

    public int getAdaptationPeriod() {
        return experiment.optInt("adaptationLength", 20);
    }

    public int getStimulusPeriod() {
        return experiment.optInt("stimulusLength", 20);
    }

    public String getClipAlignment() {
        return experiment.optString("clipAlignment", "center");
    }

    public ExternalCondition getExternalCondition() {
        ExternalCondition externalCondition = new ExternalCondition();

        if (experiment.has("externalCondition")) {
            try {
                JSONObject conditionObj = experiment.getJSONObject("externalCondition");
                externalCondition.setLabel(conditionObj.getString("label"));

                JSONArray options = conditionObj.getJSONArray("options");
                for (int i = 0; i < options.length(); i++) {
                    externalCondition.addOption(options.getString(i));
                }

                return externalCondition;
            } catch (JSONException je) {
                Log.e(LOG_TAG, "Could not parse externalCondition field: " + je);
            }
        }

        externalCondition.setLabel("Fabric");
        externalCondition.addOption("on");
        externalCondition.addOption("off");

        return externalCondition;
    }

    public int getRepetitions() {
        int repetitions = experiment.optInt("repetitions", 1);

        if (repetitions < 1) {
            return 1;
        }

        return repetitions;
    }

    public ArrayList<Trial> getShuffledTrials(String externalCondition, int counterbalance) {
        try {
            JSONArray trials = experiment.getJSONArray("trials");
            ArrayList<Trial> initialTrials = new ArrayList<>(trials.length());

            for (int i=0; i<trials.length(); i++) {
                JSONObject trialObject = (JSONObject) trials.get(i);

                initialTrials.add(new Trial(
                        trialObject.optString("audioFile", null),
                        trialObject.getString("condition"),
                        trialObject.optInt("intensity", 0),
                        externalCondition
                ));
            }

            int repetitions = this.getRepetitions();
            ArrayList<Trial> listWithRepetitions = new ArrayList<>(trials.length() * repetitions);

            for (int i=0; i<repetitions; i++) {
                listWithRepetitions.addAll(initialTrials);
            }

            Trial counterBalanceTrial = listWithRepetitions.remove(counterbalance);
            Collections.shuffle(listWithRepetitions);

            ArrayList<Trial> shuffledList = new ArrayList<>(trials.length() * repetitions);

            shuffledList.add(counterBalanceTrial);
            shuffledList.addAll(listWithRepetitions);

            return shuffledList;
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Could not parse JSON: " + je);
            return new ArrayList<>();
        }
    }
}
