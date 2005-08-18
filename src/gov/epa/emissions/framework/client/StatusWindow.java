package gov.epa.emissions.framework.client;

import java.awt.Dimension;

import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

public class StatusWindow extends EmfInteralFrame implements StatusView {

    public StatusWindow(User user, StatusServices statusServices) {
        super("Status Messages");

        setSize(new Dimension(100, 100));
        super.setClosable(false);
        super.setIconifiable(false);
        super.setMaximizable(false);
    }

    public void close() {
        super.dispose();
    }

    public void display() {
        super.setVisible(true);
    }

    public void update(Status[] messages) {
    }

    public void notifyError(String message) {        
    }

}
