package nl.cwi.dis.physiofashion.experiment;

import java.util.ArrayList;

public class ExternalCondition {
    private String label;
    private ArrayList<String> options;

    public ExternalCondition() {
        this.label = "";
        this.options = new ArrayList<>();
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void addOption(String option) {
        this.options.add(option);
    }

    public ArrayList<String> getOptions() {
        return this.options;
    }

    public String getLabel() {
        return this.label;
    }
}
