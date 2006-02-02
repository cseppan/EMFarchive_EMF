package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class AddAdminOption implements AdminOption {

    private JCheckBox isAdmin;

    public AddAdminOption() {
        isAdmin = new JCheckBox("Is Admin?");
    }

    public void add(JPanel panel) {
        panel.add(isAdmin);
    }

    public void setInAdminGroup(User user) {
        user.setAdmin(isAdmin.isSelected());
    }

}
