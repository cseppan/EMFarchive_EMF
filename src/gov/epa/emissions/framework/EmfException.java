package gov.epa.emissions.framework;

public class EmfException extends Exception {

    String details;

    public EmfException(String message, String details, Throwable cause) {
        super(message, cause);
        this.details = details;
    }

    public EmfException(String message, String details) {
        super(message);
        this.details = details;
    }

    public EmfException(String message) {
        super(message);
    }

}
