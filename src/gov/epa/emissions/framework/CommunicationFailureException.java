package gov.epa.emissions.framework;

public class CommunicationFailureException extends InfrastructureException {

    public CommunicationFailureException(String description, String details, Throwable cause) {
        super(description, details, cause);
    }

    public CommunicationFailureException(String description, String details) {
        super(description,details);
    }

    public CommunicationFailureException(String message) {
        super(message);
    }


}
