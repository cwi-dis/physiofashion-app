package nl.cwi.dis.physiofashion.experiment;

import android.os.Parcel;
import android.os.Parcelable;

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
    private boolean fabricOn;

    private Trial(Parcel in) {
        this.audioFile = in.readString();
        this.condition = in.readString();
        this.intensity = in.readInt();
        this.fabricOn = in.readInt() == 1;
    }

    public Trial(String audioFile, String condition, int intensity, boolean fabricOn) {
        this.audioFile = audioFile;
        this.condition = condition;
        this.intensity = intensity;
        this.fabricOn = fabricOn;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(audioFile);
        dest.writeString(condition);
        dest.writeInt(intensity);
        dest.writeInt(fabricOn ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean hasAudio() {
        return audioFile != null;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public int getIntensity() {
        return intensity;
    }

    public String getCondition() {
        return condition;
    }

    public boolean isFabricOn() {
        return fabricOn;
    }
}