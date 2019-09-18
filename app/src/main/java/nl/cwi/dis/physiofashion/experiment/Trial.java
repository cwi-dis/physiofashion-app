package nl.cwi.dis.physiofashion.experiment;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class encapsulates a trial within an experiment and should be initialised from an experiment
 * object. This class provides accessor methods for all trial properties. Also note that this class
 * extends the Parcelable interface, so its instances can be passed between activities.
 */
public class Trial implements Parcelable {
    public static final Parcelable.Creator<Trial> CREATOR = new Parcelable.Creator<Trial>() {
        @Override
        public Trial createFromParcel(Parcel in) {
            return new Trial(in);
        }

        @Override
        public Trial[] newArray(int size) {
            return new Trial[size];
        }
    };

    private String audioFile;
    private String condition;
    private int intensity;
    private String externalCondition;

    /**
     * Construct a new Trial object from an existing Parcel object.
     *
     * @param in Parcel object to construct trial from
     */
    private Trial(Parcel in) {
        this.audioFile = in.readString();
        this.condition = in.readString();
        this.intensity = in.readInt();
        this.externalCondition = in.readString();
    }

    /**
     * Initialise new trial object with audio file, heating/cooling condition, temperature intensity
     * and description of current external condition.
     *
     * @param audioFile Name of the audio file, if any
     * @param condition Condition to be applied for the heating element
     * @param intensity Temperature intensity for the condition
     * @param externalCondition Name for the external condition that applies for this trial
     */
    public Trial(String audioFile, String condition, int intensity, String externalCondition) {
        this.audioFile = audioFile;
        this.condition = condition;
        this.intensity = intensity;
        this.externalCondition = externalCondition;
    }

    /**
     * Write the object to a parcel.
     *
     * @param dest Destination parcel
     * @param flags Flags, ignored
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(audioFile);
        dest.writeString(condition);
        dest.writeInt(intensity);
        dest.writeString(externalCondition);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Returns whether the trial has an audio file associated to it.
     *
     * @return Whether the trial has an audio file
     */
    public boolean hasAudio() {
        return audioFile != null;
    }

    /**
     * Get the trial's audio file if available.
     *
     * @return The name of the trials audio file
     */
    public String getAudioFile() {
        return audioFile;
    }

    /**
     * The temperature intensity for this trial.
     *
     * @return Temperature intensity
     */
    public int getIntensity() {
        return intensity;
    }

    /**
     * Returns whether the trial involves heating or cooling the heating element.
     *
     * @return Condition for this trial
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Returns the state of the external condition for this trial.
     *
     * @return The external condition for this trial
     */
    public String getExternalCondition() {
        return externalCondition;
    }
}
