package nl.cwi.dis.physiofashion.experiment;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    }

    public Experiment(ArrayList<Trial> trials, String hostname, String participantId, int counterBalance) {
        this.trials = trials;
        this.hostname = hostname;
        this.participantId = participantId;
        this.counterBalance = counterBalance;
        this.currentTrial = 0;
        this.responses = new ArrayList<>(trials.size());
        this.baselineTemp = 32;
        this.adaptationPeriod = 20;
        this.stimulusPeriod = 10;
    }

    public Experiment(JSONExperiment experimentData, String participantId, int counterBalance, boolean fabricOn) {
        this.trials = experimentData.getShuffledTrials(fabricOn, counterBalance);
        this.hostname = experimentData.getHostname();
        this.participantId = participantId;
        this.counterBalance = counterBalance;
        this.currentTrial = 0;
        this.responses = new ArrayList<>(trials.size());
        this.baselineTemp = experimentData.getBaselineTemperature();
        this.adaptationPeriod = experimentData.getAdaptationPeriod();
        this.stimulusPeriod = experimentData.getStimulusPeriod();
    }

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
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<Trial> getTrials() {
        return trials;
    }

    public String getHostname() {
        return hostname;
    }

    public String getParticipantId() {
        return participantId;
    }

    public int getCounterBalance() {
        return counterBalance;
    }

    public Trial getCurrentTrial() {
        if (this.currentTrial >= this.trials.size()) {
            return null;
        }

        return this.trials.get(this.currentTrial);
    }

    public void incrementCurrentTrial() {
        this.currentTrial++;
    }

    public int getCurrentTrialIndex() {
        return this.currentTrial;
    }

    public UserResponse getCurrentUserResponse() {
        if (this.currentTrial >= this.responses.size()) {
            this.responses.add(new UserResponse());
        }

        return this.responses.get(this.currentTrial);
    }

    public ArrayList<UserResponse> getResponses() {
        return this.responses;
    }

    public int getBaselineTemp() {
        return baselineTemp;
    }

    public int getAdaptationPeriod() {
        return adaptationPeriod;
    }

    public int getStimulusPeriod() {
        return stimulusPeriod;
    }

    public void writeResponsesToFile(File targetDir) {
        ArrayList<String> lines = IntStream.range(0, this.trials.size()).mapToObj(i -> {
            Trial trial = this.trials.get(i);
            UserResponse response = this.responses.get(i);

            return String.format(
                    Locale.ENGLISH,
                    "%d,\"%s\",\"%s\",%d,%d,%.2f,%.2f,%d,%d\n",
                    i + 1,
                    this.participantId,
                    trial.getCondition(),
                    trial.getIntensity(),
                    trial.isFabricOn() ? 1 : 0,
                    response.getStimulusStarted(),
                    response.getStimulusFelt(),
                    response.getTemperatureFelt(),
                    response.getComfortLevel()
            );
        }).collect(Collectors.toCollection(ArrayList::new));

        String filename = this.participantId + "_" + (this.trials.get(0).isFabricOn() ? "fabricOn" : "fabricOff") + ".csv";
        String header = "\"trialNum\",\"participant\",\"condition\",\"intensity\",\"fabricOn\",\"stimulusStarted\",\"stimulusFelt\",\"temperatureFelt\",\"comfortLevel\"\n";

        Log.d(LOG_TAG, "Attempting to write responses to file: " + targetDir.getAbsolutePath() + "/" + filename);

        this.writeDataToFile(
                filename,
                targetDir,
                header,
                lines
        );
    }

    private String getNewFilename(String filename, File targetDir) {
        int counter = 0;

        while (true) {
            File targetFile = new File(targetDir, filename);

            if (!targetFile.exists()) {
                return filename;
            }

            counter++;
            filename = filename.replace(".csv", "") + String.format(Locale.ENGLISH, "_%02d.csv", counter);
        }
    }

    private void writeDataToFile(String filename, File targetDir, String header, ArrayList<String> lines) {
        if (lines.size() == 0) {
            return;
        }

        filename = this.getNewFilename(filename, targetDir);

        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(targetDir, filename))) {
            fileOutputStream.write(header.getBytes());

            for (String line : lines) {
                fileOutputStream.write(line.getBytes());
            }
        } catch (FileNotFoundException fnf) {
            Log.e(LOG_TAG, "File " + filename + " not found: " + fnf);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "File " + filename + " IO Exception: " + ioe);
        }
    }
}
