package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.services.EmfException;

import javax.swing.JTextField;

public class NumberFieldVerifier {

    private String message;

    public NumberFieldVerifier(String message) {
        this.message = message;
    }

    public int parseInteger(JTextField numberField) throws EmfException {
        try {
            String text = numberField.getText().trim();
            if (text.length() == 0)
                return 0;
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new EmfException(message + numberField.getName() + " should be an integer.");
        }
    }

    public double parseDouble(JTextField numberField) throws EmfException {
        try {
            String text = numberField.getText().trim();
            if (text.length() == 0)
                return 0;

            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            throw new EmfException(message + numberField.getName() + " should be a double.");
        }

    }

    public float parseFloat(JTextField numberField) throws EmfException {
        try {
            String text = numberField.getText().trim();
            if (text.length() == 0)
                return 0;
            return Float.parseFloat(text);
        } catch (NumberFormatException ex) {
            throw new EmfException(message + numberField.getName() + " should be a floating point number.");
        }
    }
}
