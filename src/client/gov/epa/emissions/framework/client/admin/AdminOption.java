package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.services.User;

import javax.swing.JPanel;

public interface AdminOption {

    void add(JPanel profileValuesPanel);

    void setInAdminGroup(User user);

}
