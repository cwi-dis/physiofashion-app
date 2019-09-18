package nl.cwi.dis.physiofashion.experiment;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class encapsulates a user response to a trial within an experiment object. This class
 * provides accessor methods for all user response properties. Also note that this class
 * extends the Parcelable interface, so its instances can be passed between activities.
 */
public class UserResponse implements Parcelable {
    public static final Parcelable.Creator<UserResponse> CREATOR = new Parcelable.Creator<UserResponse>() {
        @Override
        public UserResponse createFromParcel(Parcel in) {
            return new UserResponse(in);
        }

        @Override
        public UserResponse[] newArray(int size) {
            return new UserResponse[size];
        }
    };

    private double stimulusStarted;
    private double stimulusFelt;
    private int temperatureFelt;
    private int comfortLevel;
    private int arousal;
    private int valence;

    /**
     * Construct a new UserResponse object from an existing Parcel object.
     *
     * @param in Parcel object to construct response from
     */
    private UserResponse(Parcel in) {
        this.stimulusStarted = in.readDouble();
        this.stimulusFelt = in.readDouble();
        this.temperatureFelt = in.readInt();
        this.comfortLevel = in.readInt();
        this.arousal = in.readInt();
        this.valence = in.readInt();
    }

    /**
     * Default constructor which initialises all members to their default values.
     */
    public UserResponse() {
    }

    /**
     * Write the object to a parcel.
     *
     * @param dest Destination parcel
     * @param flags Flags, ignored
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(stimulusStarted);
        dest.writeDouble(stimulusFelt);
        dest.writeInt(temperatureFelt);
        dest.writeInt(comfortLevel);
        dest.writeInt(arousal);
        dest.writeInt(valence);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Gets the timestamp a stimulus has started.
     *
     * @return Time when the stimulus started
     **/
    public double getStimulusStarted() {
        return stimulusStarted;
    }

    /**
     * Sets the timestamp a stimulus has started.
     *
     * @param stimulusStarted Time when the stimulus started
     **/
    public void setStimulusStarted(double stimulusStarted) {
        this.stimulusStarted = stimulusStarted;
    }

    /**
     * Gets the timestamp a stimulus was felt.
     *
     * @return Time when the stimulus was felt
     **/
    public double getStimulusFelt() {
        return stimulusFelt;
    }

    /**
     * Sets the timestamp a stimulus was felt.
     *
     * @param stimulusFelt Time when the stimulus was felt
     **/
    public void setStimulusFelt(double stimulusFelt) {
        this.stimulusFelt = stimulusFelt;
    }

    /**
     * Get the temperature the user has felt.
     *
     * @return The temperature felt by the user
     */
    public int getTemperatureFelt() {
        return temperatureFelt;
    }

    /**
     * Set the temperature the user has felt.
     *
     * @param temperatureFelt The temperature felt by the user
     */
    public void setTemperatureFelt(int temperatureFelt) {
        this.temperatureFelt = temperatureFelt;
    }

    /**
     * Get the comfort level the user has felt.
     *
     * @return The comfort level felt by the user
     */
    public int getComfortLevel() {
        return comfortLevel;
    }

    /**
     * Set the comfort level the user has felt.
     *
     * @param comfortLevel The comfort level felt by the user
     */
    public void setComfortLevel(int comfortLevel) {
        this.comfortLevel = comfortLevel;
    }

    /**
     * Get the arousal felt by the user on a Likert scale.
     *
     * @return The arousal felt
     */
    public int getArousal() {
        return arousal;
    }

    /**
     * Set the arousal felt by the user on a Likert scale.
     *
     * @param arousal The arousal felt
     */
    public void setArousal(int arousal) {
        this.arousal = arousal;
    }

    /**
     * Get the valence felt by the user on a Likert scale.
     *
     * @return The valence felt
     */
    public int getValence() {
        return valence;
    }

    /**
     * Set the valence felt by the user on a Likert scale.
     *
     * @param valence The valence felt
     */
    public void setValence(int valence) {
        this.valence = valence;
    }
}
