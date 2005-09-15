package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.services.User;

import javax.swing.JCheckBox;

public class RegisterUserPanelWithAdminOption extends RegisterUserPanel {

    private JCheckBox isAdmin;

    public RegisterUserPanelWithAdminOption(PostRegisterStrategy postRegisterStrategy, RegisterCancelStrategy cancelStrategy,
            EmfWidgetContainer parent) {
        super(postRegisterStrategy, cancelStrategy, parent);

        addAdminSelection();
        super.refresh();
    }

    private void addAdminSelection() {
        this.isAdmin = new JCheckBox("Is Admin ?");
        super.addToProfilePanel(isAdmin);
    }

    protected void populateUser(User user) throws UserException {
        super.populateUser(user);
        user.setInAdminGroup(isAdmin.isSelected());
    }
}
