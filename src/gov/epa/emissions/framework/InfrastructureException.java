package gov.epa.emissions.framework;

public class InfrastructureException extends EmfException {

    public InfrastructureException(String message, Throwable details) {
        super(message, details);
    }

    public InfrastructureException(String message) {
        super(message);
    }

}
