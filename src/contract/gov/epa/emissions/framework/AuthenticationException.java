package gov.epa.emissions.framework;

public class AuthenticationException extends EmfException {

    public AuthenticationException(String description, String details, Throwable cause) {
        super(description, details,cause);
    }

    public AuthenticationException(String description, String details) {
        super(description,details);
    }

    public AuthenticationException(String message) {
        super(message);
    }

}
