package gov.epa.emissions.framework;

public class EmfException extends Exception {

    String details;
    
    public EmfException(String description, String details, Throwable cause) {
        super(description, cause);
        this.details = details;
    }

    public EmfException(String description, String details) {
        super(description);
        this.details = details;
    }

    public EmfException(String message) {
        super(message);
    }
    
}
