package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.services.User;

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
        user.setInAdminGroup(isAdmin.isSelected());
    }

}
