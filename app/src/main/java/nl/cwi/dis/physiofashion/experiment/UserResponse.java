package nl.cwi.dis.physiofashion.experiment;

import android.os.Parcel;
import android.os.Parcelable;

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

    private UserResponse(Parcel in) {
        this.stimulusStarted = in.readDouble();
        this.stimulusFelt = in.readDouble();
        this.temperatureFelt = in.readInt();
        this.comfortLevel = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(stimulusStarted);
        dest.writeDouble(stimulusFelt);
        dest.writeInt(temperatureFelt);
        dest.writeInt(comfortLevel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public double getStimulusStarted() {
        return stimulusStarted;
    }

    public void setStimulusStarted(double stimulusStarted) {
        this.stimulusStarted = stimulusStarted;
    }

    public double getStimulusFelt() {
        return stimulusFelt;
    }

    public void setStimulusFelt(double stimulusFelt) {
        this.stimulusFelt = stimulusFelt;
    }

    public int getTemperatureFelt() {
        return temperatureFelt;
    }

    public void setTemperatureFelt(int temperatureFelt) {
        this.temperatureFelt = temperatureFelt;
    }

    public int getComfortLevel() {
        return comfortLevel;
    }

    public void setComfortLevel(int comfortLevel) {
        this.comfortLevel = comfortLevel;
    }
}
