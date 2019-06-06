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
    private int currentTrial;
    private ArrayList<UserResponse> responses;

    private Experiment(Parcel in) {
        this.trials = new ArrayList<>();
        in.readTypedList(this.trials, Trial.CREATOR);

        this.hostname = in.readString();
        this.participantId = in.readString();
        this.counterBalance = in.readInt();
        this.currentTrial = in.readInt();

        this.responses = new ArrayList<>();
        in.readTypedList(this.responses, UserResponse.CREATOR);
    }

    public Experiment(ArrayList<Trial> trials, String hostname, String participantId, int counterBalance) {
        this.trials = trials;
        this.hostname = hostname;
        this.participantId = participantId;
        this.counterBalance = counterBalance;
        this.currentTrial = 0;
        this.responses = new ArrayList<>(trials.size());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(trials);
        dest.writeString(hostname);
        dest.writeString(participantId);
        dest.writeInt(counterBalance);
        dest.writeInt(currentTrial);
        dest.writeTypedList(responses);
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

    public UserResponse getCurrentUserResponse() {
        return this.responses.get(this.currentTrial);
    }

    public ArrayList<UserResponse> getResponses() {
        return this.responses;
    }
}
