package gov.epa.emissions.framework;

public class EmfException extends Exception {

    public EmfException(String message, Throwable details) {
        super(message, details);
    }

    public EmfException(String message) {
        super(message);
    }

}
