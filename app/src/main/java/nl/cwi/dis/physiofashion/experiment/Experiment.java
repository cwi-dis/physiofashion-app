package nl.cwi.dis.physiofashion.experiment;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Experiment implements Parcelable {
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

    private Experiment(Parcel in) {
        this.trials = new ArrayList<>();
        in.readTypedList(this.trials, Trial.CREATOR);

        this.hostname = in.readString();
        this.participantId = in.readString();
        this.counterBalance = in.readInt();
    }

    public Experiment(ArrayList<Trial> trials, String hostname, String participantId, int counterBalance) {
        this.trials = trials;
        this.hostname = hostname;
        this.participantId = participantId;
        this.counterBalance = counterBalance;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(trials);
        dest.writeString(hostname);
        dest.writeString(participantId);
        dest.writeInt(counterBalance);
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
}