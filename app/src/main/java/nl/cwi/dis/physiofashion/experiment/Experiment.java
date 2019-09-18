package nl.cwi.dis.physiofashion.experiment;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class encapsulates an experiment and should be initialised with an ExperimentParser object,
 * which extracts the data from a JSON file. This class provides accessor methods for all experiment
 * properties and has methods for storing user responses. It also has methods for writing the final
 * responses to a file on the device's storage. Also note that this class extends the Parcelable
 * interface, so its instances can be passed between activities.
 */
public class Experiment implements Parcelable {
    private static final String LOG_TAG = "Experiment";

    public static final Parcelable.Creator<Experiment> CREATOR = new Parcelable.Creator<Experiment>() {
        @Override
        public Experiment createFromParcel(Parcel in) {
            return new Experiment(in);
        }

        @Override
        public Experiment[] newArray(int size) {
            return new Experiment[size];
        }
    };

    private ArrayList<Trial> trials;
    private String hostname;
    private String participantId;
    private int counterBalance;
    private int currentTrial;
    private ArrayList<UserResponse> responses;
    private int baselineTemp;
    private int adaptationPeriod;
    private int stimulusPeriod;
    private String clipAlignment;
    private double alignmentCorrection;
    private int breakDuration;
    private ArrayList<Integer> breakAfter;
    private boolean hasExternalCondition;
    private String questionType;

    /**
     * Construct a new Experiment object from an existing Parcel object.
     *
     * @param in Parcel object to construct experiment from
     */
    private Experiment(Parcel in) {
        this.trials = new ArrayList<>();
        in.readTypedList(this.trials, Trial.CREATOR);

        this.hostname = in.readString();
        this.participantId = in.readString();
        this.counterBalance = in.readInt();
        this.currentTrial = in.readInt();

        this.responses = new ArrayList<>();
        in.readTypedList(this.responses, UserResponse.CREATOR);

        this.baselineTemp = in.readInt();
        this.adaptationPeriod = in.readInt();
        this.stimulusPeriod = in.readInt();

        this.clipAlignment = in.readString();
        this.alignmentCorrection = in.readDouble();

        this.breakDuration = in.readInt();
        this.breakAfter = new ArrayList<>();
        in.readList(this.breakAfter, null);

        this.hasExternalCondition = in.readInt() == 1;
        this.questionType = in.readString();
    }

    /**
     * Construct new experiment from an ExperimentParser object including counterbalance id and
     * external condition.
     *
     * @param experimentParser ExperimentParser from which to extract the data from
     * @param participantId ID of participant for experiment
     * @param counterBalance Index of trial to use as counterbalance
     * @param externalCondition Name of the first external condition
     */
    public Experiment(ExperimentParser experimentParser, String participantId, int counterBalance, String externalCondition) {
        this.trials = experimentParser.getShuffledTrials(externalCondition, counterBalance);
        this.hostname = experimentParser.getHostname();
        this.participantId = participantId;
        this.counterBalance = counterBalance;
        this.currentTrial = 0;
        this.responses = new ArrayList<>(trials.size());
        this.baselineTemp = experimentParser.getBaselineTemperature();
        this.adaptationPeriod = experimentParser.getAdaptationPeriod();
        this.stimulusPeriod = experimentParser.getStimulusPeriod();
        this.clipAlignment = experimentParser.getClipAlignment();
        this.alignmentCorrection = experimentParser.getAlignmentCorrection();
        this.breakDuration = experimentParser.getPauseDuration();
        this.breakAfter = experimentParser.getPauseIndices();
        this.hasExternalCondition = experimentParser.getExternalCondition() != null;
        this.questionType = experimentParser.getQuestionType();
    }

    /**
     * Write the object to a parcel.
     *
     * @param dest Destination parcel
     * @param flags Flags, ignored
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(trials);
        dest.writeString(hostname);
        dest.writeString(participantId);
        dest.writeInt(counterBalance);
        dest.writeInt(currentTrial);
        dest.writeTypedList(responses);
        dest.writeInt(baselineTemp);
        dest.writeInt(adaptationPeriod);
        dest.writeInt(stimulusPeriod);
        dest.writeString(clipAlignment);
        dest.writeDouble(alignmentCorrection);
        dest.writeInt(breakDuration);
        dest.writeList(breakAfter);
        dest.writeInt(hasExternalCondition ? 1 : 0);
        dest.writeString(questionType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Get list of trials.
     *
     * @return List of trials as ArrayList
     */
    public ArrayList<Trial> getTrials() {
        return trials;
    }

    /**
     * Get hostname
     *
     * @return The hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Get participant ID.
     *
     * @return ID of participant
     */
    public String getParticipantId() {
        return participantId;
    }

    /**
     * Get index of trial to use as counterbalance.
     *
     * @return Counterbalance index
     */
    public int getCounterBalance() {
        return counterBalance;
    }

    /**
     * Get current trial. The method returns `null` if we're past the last trial. This can be used
     * to determine whether the end of the experiment has been reached.
     *
     * @return Current trial, or `null` if there are no more trials
     */
    public Trial getCurrentTrial() {
        if (this.currentTrial >= this.trials.size()) {
            return null;
        }

        return this.trials.get(this.currentTrial);
    }

    /**
     * Increment trial index, i.e. move on to the next trial.
     */
    public void incrementCurrentTrial() {
        this.currentTrial++;
    }

    /**
     * Get current trial index.
     *
     * @return The current trial index
     */
    public int getCurrentTrialIndex() {
        return this.currentTrial;
    }

    /**
     * Return whether the experiment should pause. This is done by iterating the list of pause
     * indices and comparing them to the current trial index.
     *
     * @return Whether the experiment should pause
     */
    public boolean shouldExperimentPause() {
        List<Integer> breaks = this.breakAfter.stream().filter(n -> n == this.getCurrentTrialIndex() - 1).collect(Collectors.toList());
        return breaks.size() == 1;
    }

    /**
     * Return whether the experiment should pause so the experimenter can change the external
     * condition. This is done after half of the trials have been completed. If there is no external
     * condition, this method always returns false.
     *
     * @return Whether the external condition should change
     */
    public boolean shouldExternalConditionChange() {
        // If there is no external condition, always return false
        if (!this.hasExternalCondition) {
            return false;
        }

        // Return true if we're at the halfway point
        return this.getCurrentTrialIndex() == (this.getTrials().size() / 2);
    }

    /**
     * Returns the duration of the pause.
     *
     * @return The pause duration
     */
    public int getBreakDuration() {
        return breakDuration;
    }

    public UserResponse getCurrentUserResponse() {
        if (this.currentTrial >= this.responses.size()) {
            this.responses.add(new UserResponse());
        }

        return this.responses.get(this.currentTrial);
    }

    /**
     * Get a list of user responses.
     *
     * @return The user responses
     */
    public ArrayList<UserResponse> getResponses() {
        return this.responses;
    }

    /**
     * Get the baseline temperature.
     *
     * @return The baseline temperature
     */
    public int getBaselineTemp() {
        return baselineTemp;
    }

    /**
     * Get the length of the adaptation period.
     *
     * @return The length of the adaptation period
     */
    public int getAdaptationPeriod() {
        return adaptationPeriod;
    }

    /**
     * Get the length of the stimulus period.
     *
     * @return The length of the stimulus period
     */
    public int getStimulusPeriod() {
        return stimulusPeriod;
    }

    /**
     * Get the clip alignment for audio clips.
     *
     * @return The clip alignment
     */
    public String getClipAlignment() {
        return clipAlignment;
    }

    /**
     * Get the alignment correction for audio clips, i.e. offset after a clip has been aligned.
     *
     * @return The alignment correction
     */
    public double getAlignmentCorrection() {
        return alignmentCorrection;
    }

    /**
     * Get the question type for the rating activity. Should be either `likert` or `manikin`.
     *
     * @return The question type
     */
    public String getQuestionType() {
        return questionType;
    }

    /**
     * Writes the user responses to a CSV file at the given path. Returns the path and filename the
     * file has been written to as a string or `null` on error.
     *
     * @param targetDir The target directory the file should be written to
     * @return The path of the file written or `null` on error
     */
    public String writeResponsesToFile(File targetDir) {
        // Map over all indices of the response and collect the string into an ArrayList
        ArrayList<String> lines = IntStream.range(0, this.trials.size()).mapToObj(i -> {
            // Get trial and corresponding response
            Trial trial = this.trials.get(i);
            UserResponse response = this.responses.get(i);

            // Format line for each trial and response pair
            return String.format(
                    Locale.ENGLISH,
                    "%d,\"%s\",\"%s\",%d,\"%s\",\"%s\",%.2f,%.2f,%d,%d,%d,%d\n",
                    i + 1,
                    this.participantId,
                    trial.getCondition(),
                    trial.getIntensity(),
                    trial.getExternalCondition(),
                    trial.getAudioFile(),
                    response.getStimulusStarted(),
                    response.getStimulusFelt(),
                    response.getTemperatureFelt(),
                    response.getComfortLevel(),
                    response.getArousal(),
                    response.getValence()
            );
        }).collect(Collectors.toCollection(ArrayList::new));

        // Tentative filename for the output file
        String filename = this.participantId + ".csv";
        // Header for the CSV file
        String header = "\"trialNum\",\"participant\",\"condition\",\"intensity\",\"externalCondition\",\"audioFile\",\"stimulusStarted\",\"stimulusFelt\",\"temperatureFelt\",\"comfortLevel\",\"arousal\",\"valence\"\n";
        lines.add(0, header);

        Log.d(LOG_TAG, "Attempting to write responses to file: " + targetDir.getAbsolutePath() + File.separator + filename);

        // Write file and return final path
        return this.writeDataToFile(
                filename,
                targetDir,
                lines
        );
    }

    /**
     * Takes a filename and a directory and checks whether this filename exists in that directory.
     * If not, the filename is returned as it was passed in. Otherwise, the original filename is
     * altered by appending a number to it. This new filename is checked for existence again. This
     * process is repeated until a filename which is not taken yet is found. This filename is then
     * returned.
     *
     * @param filename Tentative filename
     * @param targetDir Directory we want to save the file in
     * @return The original filename if available, otherwise the name with a number attached to it
     */
    private String getNewFilename(String filename, File targetDir) {
        int counter = 0;

        while (true) {
            // Initialise a file with the name in the target directory
            File targetFile = new File(targetDir, filename);

            // If the file does not exist, return the free filename
            if (!targetFile.exists()) {
                return filename;
            }

            // Increment the counter and append it to the filename, generating a new filename
            counter++;
            filename = filename.replace(".csv", "") + String.format(Locale.ENGLISH, "_%02d.csv", counter);
        }
    }

    /**
     * This method writes a list of strings to the given directory under the given file name. If
     * the chosen filename already exists, a new filename is generated by appending a number to the
     * name. The absolute path of the newly written file is returned, or `null` on error.
     *
     * @param filename Tentative filename
     * @param targetDir Target directory
     * @param lines Lines to write to the file
     * @return The absolute path the file was saved under, or `null` on error
     */
    private String writeDataToFile(String filename, File targetDir, ArrayList<String> lines) {
        // Don't do anything if there are no lines to write
        if (lines.size() == 0) {
            return null;
        }

        // Get free filename
        filename = this.getNewFilename(filename, targetDir);

        // Open stream
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(targetDir, filename))) {
            // Write to stream line by line
            for (String line : lines) {
                fileOutputStream.write(line.getBytes());
            }

            // Return save path
            return targetDir + File.separator + filename;
        } catch (FileNotFoundException fnf) {
            Log.e(LOG_TAG, "File " + filename + " not found: " + fnf);
            return null;
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "File " + filename + " IO Exception: " + ioe);
            return null;
        }
    }
}
