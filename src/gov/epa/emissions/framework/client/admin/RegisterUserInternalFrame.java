package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.services.UserServices;

public class RegisterUserInternalFrame extends EmfInteralFrame implements EmfWidgetContainer {

    private RegisterUserPanel view;

    public RegisterUserInternalFrame(UserServices userAdmin, PostRegisterStrategy postRegisterStrategy) {
        super("Register New User");

        view = new RegisterUserPanelWithAdminOption(userAdmin, postRegisterStrategy, this);

        super.setSize(view.getSize());
        super.getContentPane().add(view);
    }

    public void close() {
        super.dispose();
    }

    public RegisterUserView getView() {
        return view;
    }

    public void display() {
        super.show();
    }

}
