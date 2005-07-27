package gov.epa.emissions.framework;

public class SessionTimedOutException extends EmfException {

    public SessionTimedOutException(String description, String details, Throwable cause) {
        super(description, details,cause);
    }

    public SessionTimedOutException(String description, String details) {
        super(description,details);
    }

    public SessionTimedOutException(String message) {
        super(message);
    }

}
