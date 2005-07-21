package gov.epa.emissions.framework;

public class SessionTimedOutException extends EmfException {

    public SessionTimedOutException(String message, Throwable details) {
        super(message, details);
    }

    public SessionTimedOutException(String message) {
        super(message);
    }

}
