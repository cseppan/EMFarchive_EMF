package gov.epa.emissions.framework;

public class CommunicationFailureException extends InfrastructureException {

    public CommunicationFailureException(String message, Throwable details) {
        super(message, details);
    }

    public CommunicationFailureException(String message) {
        super(message);
    }

}
