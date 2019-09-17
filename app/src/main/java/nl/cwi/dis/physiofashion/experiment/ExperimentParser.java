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
import java.util.stream.Collectors;

/**
 * Class responsible for interpreting a given file object as a JSON file and extract values
 * instrumental for running an experiment from it. Most importantly, it generates the shuffled list
 * of trials for the experiment, taking into account external conditions, repetitions and
 * counterbalance trials.
 */
public class ExperimentParser {
    private static final String LOG_TAG = "ExperimentParser";
    private JSONObject experiment;

    /**
     * Initialise a new ExperimentParser object. Methods in this class can then be used to extract
     * desired values from it.
     *
     * @param experimentFile JSON file containing the experiment data
     */
    public ExperimentParser(File experimentFile) {
        this.experiment = this.readExperimentFromFile(experimentFile);
    }

    /**
     * Loads the given file and tries to parse it as JSON. If the file either cannot be found or
     * cannot be parsed correctly, `null` is returned.
     *
     * @param experimentFile JSON file containing the experiment data
     */
    private JSONObject readExperimentFromFile(File experimentFile) {
        try {
            // Read the entire file into a string
            Scanner scanner = new Scanner(experimentFile);
            String fileContents = scanner.useDelimiter("\\A").next();
            scanner.close();

            // Try and parse the string as a JSON object
            return new JSONObject(fileContents);
        } catch (FileNotFoundException fnf) {
            Log.e(LOG_TAG, "Experiment file not found: " + fnf);
            return null;
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Could not parse JSON object: " + je);
            return null;
        }
    }

    /**
     * Returns whether the experiment is valid, i.e. could be loaded and parsed correctly
     *
     * @return Whether the experiment is valid
     */
    public boolean isValidExperiment() {
        return experiment != null;
    }

    /**
     * Returns the hostname given in the JSON.
     *
     * @return The hostname or `null` by default
     */
    public String getHostname() {
        return experiment.optString("hostname", null);
    }

    /**
     * Returns the baseline temperature given in the JSON.
     *
     * @return The baseline temperature or 32 by default
     */
    public int getBaselineTemperature() {
        return experiment.optInt("baselineTemperature", 32);
    }

    /**
     * Returns the length of the adaptation period given in the JSON.
     *
     * @return The adaptation length in seconds or 20 by default
     */
    public int getAdaptationPeriod() {
        return experiment.optInt("adaptationLength", 20);
    }

    /**
     * Returns the length of the stimulus period given in the JSON.
     *
     * @return The stimulus length in seconds or 20 by default
     */
    public int getStimulusPeriod() {
        return experiment.optInt("stimulusLength", 20);
    }

    /**
     * Returns the clip alignment, usually 'start', 'center' or 'end'.
     *
     * @return Alignment for audio clips or 'center' by default
     */
    public String getClipAlignment() {
        return experiment.optString("clipAlignment", "center");
    }

    /**
     * Returns the alignment correction for audio clips.
     *
     * @return The alignment correction in seconds or zero by default
     */
    public double getAlignmentCorrection() {
        return experiment.optDouble("alignmentCorrection", 0);
    }

    /**
     * Tries to retrieve data from the field `externalCondition` in the JSON and returns it as an
     * object. If no such field could be found or if the JSON could not be parsed correctly, `null`
     * is returned.
     *
     * @return The external condition options wrapped in an ExternalCondition object or `null`
     */
    public ExternalCondition getExternalCondition() {
        // Check whether JSON has a key `externalCondition`
        if (experiment.has("externalCondition")) {
            ExternalCondition externalCondition = new ExternalCondition();

            try {
                JSONObject conditionObj = experiment.getJSONObject("externalCondition");
                // Extract label from externalCondition field and set it on the object
                externalCondition.setLabel(conditionObj.getString("label"));

                // Extract options from externalCondition field and add them to the object
                JSONArray options = conditionObj.getJSONArray("options");
                for (int i = 0; i < options.length(); i++) {
                    externalCondition.addOption(options.getString(i));
                }

                return externalCondition;
            } catch (JSONException je) {
                Log.e(LOG_TAG, "Could not parse externalCondition field: " + je);
            }
        }

        // Return null if the JSON had no `externalCondition` field or could not be parsed correctly
        return null;
    }

    /**
     * Get the number of repetitions for the experiment. Note that a value of 1 means that the
     * trials shall be run exactly once.
     *
     * @return The number of times the trials shall be repeated, defaults to one
     */
    public int getRepetitions() {
        // Get value of key repetitions or one by default
        int repetitions = experiment.optInt("repetitions", 1);

        // Returns 1 if value is zero or below
        if (repetitions < 1) {
            return 1;
        }

        return repetitions;
    }

    /**
     * Returns the value of the `questionType` field, or 'likert' of none is present.
     *
     * @return The question type or 'likert' by default
     */
    public String getQuestionType() {
        try {
            // Retrieve key questionTyep from JSON
            return experiment.getString("questionType");
        } catch (JSONException je) {
            return "likert";
        }
    }

    /**
     * Returns the duration found under the `duration` key in the `pauses` object. If any of the
     * keys do not exist, zero is returned instead.
     *
     * @return Duration of pause or zero if not present
     */
    public int getPauseDuration() {
        try {
            // Get pauses object
            JSONObject pauses = experiment.getJSONObject("pauses");
            // Try and return value under duration
            return pauses.optInt("duration", 0);
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Could not parse pauses field: " + je);
        }

        // Return zero if an exception occurred
        return 0;
    }

    /**
     * Retrieves indices of pauses in between trials. An experiment can have multiple pause screens,
     * which are indicated by a zero-based index after which trial the pause screen is to be
     * displayed, i.e. an index of zero specifies that the pause screen is to be displayed after the
     * first trial. If no pauses were specified, an empty list is returned.
     *
     * @return The indices of the trials after which the pause screen shall be displayed.
     */
    public ArrayList<Integer> getPauseIndices() {
        // Initialise list of pauses
        ArrayList<Integer> pauseIndices = new ArrayList<>();

        try {
            // Retrieve pauses object and pauseAfter key under it as array
            JSONObject pauses = experiment.getJSONObject("pauses");
            JSONArray pauseAfter = pauses.getJSONArray("pauseAfter");

            // Iterate over array and add indices to output list
            for (int i=0; i<pauseAfter.length(); i++) {
                pauseIndices.add(pauseAfter.getInt(i));
            }
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Could not parse pauses field: " + je);
            // Return empty list of JSON is invalid
            return new ArrayList<>();
        }

        // Return list of indices
        return pauseIndices;
    }

    /**
     * Sorts the list of external condition options, making sure that the condition option given as
     * parameter comes first. If the experiment config does not have any external conditions a list
     * with a single item containing the empty string is returned.
     *
     * @param first Condition which should be the first item of the list
     * @return Sorted list of external conditions with the on passed as param in first place
     */
    private ArrayList<String> sortExternalConditionOptions(String first) {
        ArrayList<String> result = new ArrayList<>();

        // Return a list with a single item if the epxeriment does not specify external conditions
        if (this.getExternalCondition() == null) {
            result.add("");
            return result;
        }

        // Get external condition options
        ArrayList<String> externalConditions = this.getExternalCondition().getOptions();

        // Add given item to head of list
        result.add(first);
        // Add rest of external condition options to output and make sure to filter out condition
        // option given by the param `first`
        result.addAll(externalConditions.stream().filter(x -> x.compareTo(first) != 0).collect(Collectors.toList()));

        return result;
    }

    /**
     * This method extracts the trials from the experiment file under the key `trials` and returns
     * a shuffled list of trials, taking into account external conditions and counterbalance trials.
     * If the trials from the experiment config could not be parsed correctly, an empty list is
     * returned.
     *
     * @param firstExternalCondition The first external condition to be examined
     * @param counterbalance Index of the trial to be used as counterbalance
     * @return A shuffled list of trials taking into account counterbalance and repetitions
     */
    public ArrayList<Trial> getShuffledTrials(String firstExternalCondition, int counterbalance) {
        try {
            // Get value of key `trials` as an array
            JSONArray trials = experiment.getJSONArray("trials");
            // Initialise list of final trials
            ArrayList<Trial> finalList = new ArrayList<>();

            // Repeat initialisation of trials, counterbalance and shuffling for each external
            // condition, starting with the external condition given by `firstExternalCondition`
            for (String externalCondition : this.sortExternalConditionOptions(firstExternalCondition)) {
                ArrayList<Trial> initialTrials = new ArrayList<>(trials.length());

                // Iterate over trials array
                for (int i = 0; i < trials.length(); i++) {
                    // Get each trials object
                    JSONObject trialObject = (JSONObject) trials.get(i);

                    // Add trials as instance of Trial to list, with audio file, condition (`warm`
                    // or `cool`) and intensity (temperature delta)
                    initialTrials.add(new Trial(
                            trialObject.optString("audioFile", null),
                            trialObject.getString("condition"),
                            trialObject.optInt("intensity", 0),
                            externalCondition
                    ));
                }

                // Get number of repetitions and initialise new list
                int repetitions = this.getRepetitions();
                ArrayList<Trial> listWithRepetitions = new ArrayList<>(trials.length() * repetitions);

                // Keep duplicating list of initial trials and add it to list
                for (int i = 0; i < repetitions; i++) {
                    listWithRepetitions.addAll(initialTrials);
                }

                // Remove trial at index chosen for counterbalance
                Trial counterBalanceTrial = listWithRepetitions.remove(counterbalance);
                // Shuffle list
                Collections.shuffle(listWithRepetitions);

                // Initialise shuffled list
                ArrayList<Trial> shuffledList = new ArrayList<>(trials.length() * repetitions);

                // Add counterbalance trial and shuffled list to new list
                shuffledList.add(counterBalanceTrial);
                shuffledList.addAll(listWithRepetitions);

                // Add shuffled list to output list
                finalList.addAll(shuffledList);
            }

            // Return final list
            return finalList;
        } catch (JSONException je) {
            Log.e(LOG_TAG, "Could not parse JSON: " + je);
            // Return empty list if the JSON could not be parsed/processed correctly
            return new ArrayList<>();
        }
    }
}
