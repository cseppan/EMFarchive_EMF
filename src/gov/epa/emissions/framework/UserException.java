package gov.epa.emissions.framework;

public class UserException extends EmfException {

    public UserException(String message, Throwable details) {
        super(message, details);
    }

    public UserException(String message) {
        super(message);
    }

}
