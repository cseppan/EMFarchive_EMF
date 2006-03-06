package gov.epa.emissions.framework;

import java.rmi.RemoteException;

public class EmfException extends RemoteException {

    String details;

    public EmfException() {//
    }

    public EmfException(String message) {
        super(message);
        this.details = message;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

}
