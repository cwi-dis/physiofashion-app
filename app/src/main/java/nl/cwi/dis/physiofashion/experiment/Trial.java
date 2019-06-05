package nl.cwi.dis.physiofashion.experiment;

public class Trial {
    private String audioFile;
    private String condition;
    private String intensity;

    public Trial(String audioFile, String condition, String intensity) {
        this.audioFile = audioFile;
        this.condition = condition;
        this.intensity = intensity;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public String getIntensity() {
        return intensity;
    }

    public String getCondition() {
        return condition;
    }
}
