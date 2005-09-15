package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.services.User;

import javax.swing.JPanel;

public class NoAdminOption implements AdminOption {

    public void add(JPanel profileValuesPanel) {
        // Note: No Op
    }

    public void setInAdminGroup(User user) {
        // Note: No Op
    }

}
