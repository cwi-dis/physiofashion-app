package nl.cwi.dis.physiofashion.experiment;

import java.util.ArrayList;

/**
 * This class is meant to encapsulate an external condition, i.e. a condition which needs to be
 * controlled externally. In this specific case, it can mean the location of the heating element
 * on the body. This is not something that the app can change, i.e. it has to be changed by the
 * experimenter manually (by physically changing the location of the heating element on the
 * subject's body). This class allows for setting a custom label (e.g. "Location of heating
 * element") and custom names for the options that can be selected in the UI (e.g. "Chest" or
 * "Arm").
 */
public class ExternalCondition {
    private String label;
    private ArrayList<String> options;

    /**
     * Initialise new ExternalCondition object with default settings.
     */
    public ExternalCondition() {
        this.label = "";
        this.options = new ArrayList<>();
    }

    /**
     * Set the label for the external condition.
     *
     * @param label The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Add an option for the external condition.
     *
     * @param option The option
     */
    public void addOption(String option) {
        this.options.add(option);
    }

    /**
     * Get all options for the condition.
     *
     * @return A list of options
     */
    public ArrayList<String> getOptions() {
        return this.options;
    }

    /**
     * Get the label for the condition.
     *
     * @return The label for the condition
     */
    public String getLabel() {
        return this.label;
    }
}
