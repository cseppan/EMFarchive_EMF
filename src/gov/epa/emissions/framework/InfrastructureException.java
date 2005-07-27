package gov.epa.emissions.framework;

public class InfrastructureException extends EmfException {

    public InfrastructureException(String description, String details, Throwable cause) {
        super(description, details,cause);
    }

    public InfrastructureException(String description, String details) {
        super(description,details);
    }

    public InfrastructureException(String message) {
        super(message);
    }

}
