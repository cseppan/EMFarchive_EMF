package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import javax.swing.JCheckBox;

public class RegisterUserPanelWithAdminOption extends RegisterUserPanel {

    private JCheckBox isAdmin;

    public RegisterUserPanelWithAdminOption(UserServices userAdmin, PostRegisterStrategy postRegisterStrategy,
            EmfWidgetContainer parent) {
        super(postRegisterStrategy, parent);

        this.isAdmin = new JCheckBox("Administrator");
        super.profileValuesPanel.add(isAdmin);

        super.refresh();
    }

    protected void populateUser(User user) throws UserException {
        super.populateUser(user);
        user.setInAdminGroup(isAdmin.isSelected());
    }
}
