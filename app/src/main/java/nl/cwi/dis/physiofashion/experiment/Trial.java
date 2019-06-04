package nl.cwi.dis.physiofashion.experiment;

public class Trial {
    private String condition;
    private String intensity;

    public Trial(String condition, String intensity) {
        this.condition = condition;
        this.intensity = intensity;
    }

    public String getIntensity() {
        return intensity;
    }

    public String getCondition() {
        return condition;
    }
}
