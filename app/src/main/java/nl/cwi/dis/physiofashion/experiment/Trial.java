package nl.cwi.dis.physiofashion.experiment;

public class Trial {
    private String audioFile;
    private String condition;
    private int intensity;

    public Trial(String audioFile, String condition, int intensity) {
        this.audioFile = audioFile;
        this.condition = condition;
        this.intensity = intensity;
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
}
