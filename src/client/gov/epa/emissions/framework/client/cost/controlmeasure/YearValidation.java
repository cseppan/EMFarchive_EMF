package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;

public class YearValidation {

    private String name;

    private int startYear;

    private int endYear;

    public YearValidation(String name) {
        this.name = name;
        this.startYear = 1980;
        this.endYear = 2100;
    }

    public int value(String text) throws EmfException {
        int value = intValue(text);
        if (text.length() != 4)
            throw new EmfException(message(name));
        if (value < 1980 || value > 2100) {
            throw new EmfException(message(name));
        }
        return value;
    }

    private int intValue(String text) throws EmfException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new EmfException(message(name));
        }
    }

    private String message(String name) {
        return "Please enter a " + name + "(four digit integer) between " + startYear + " and " + endYear + ".";
    }

}
