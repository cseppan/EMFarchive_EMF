package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.services.EmfException;

import javax.swing.JTextField;

public class NumberFieldVerifier {
    
    private String message;
    
    public NumberFieldVerifier(String message) {
        this.message = message;
    }
    
    public int parseInteger(JTextField numberField) throws EmfException {
        int val = 0;
        try {
            val = Integer.parseInt(numberField.getText());
        } catch (NumberFormatException ex) {
            throw new EmfException(message + numberField.getName() + " field should be an integer number.");
        }
        
        return val;
    }
    
    public double parseDouble(JTextField numberField) throws EmfException {
        double val = 0;
        try {
            val = Double.parseDouble(numberField.getText());
        } catch (NumberFormatException ex) {
            throw new EmfException(message + numberField.getName() + " field should be a double number.");
        }
        
        return val;
    }
    
    public float parseFloat(JTextField numberField) throws EmfException {
        float val = 0;
        try {
            val = Float.parseFloat(numberField.getText());
        } catch (NumberFormatException ex) {
            throw new EmfException(message + numberField.getName() + " field should be a floating point number.");
        }

        return val;
    }
}
