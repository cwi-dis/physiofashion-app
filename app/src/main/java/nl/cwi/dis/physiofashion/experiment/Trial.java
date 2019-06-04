package nl.cwi.dis.physiofashion.experiment;

public class Trial {
    private boolean fabricOnSkin;
    private String condition;
    private String intensity;

    public Trial(boolean fabricOnSkin, String condition, String intensity) {
        this.fabricOnSkin = fabricOnSkin;
        this.condition = condition;
        this.intensity = intensity;
    }

    public boolean isFabricOnSkin() {
        return fabricOnSkin;
    }

    public String getIntensity() {
        return intensity;
    }

    public String getCondition() {
        return condition;
    }
}
