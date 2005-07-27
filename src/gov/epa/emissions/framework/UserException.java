package gov.epa.emissions.framework;

public class UserException extends EmfException {

    public UserException(String description, String details, Throwable cause) {
        super(description, details,cause);
    }

    public UserException(String description, String details) {
        super(description,details);
    }

    public UserException(String message) {
        super(message);
    }

}
