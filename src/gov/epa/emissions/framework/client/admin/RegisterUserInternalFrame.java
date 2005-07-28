package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

public class RegisterUserInternalFrame extends EmfInteralFrame implements EmfWidgetContainer {

    private RegisterUserPanel view;

    public RegisterUserInternalFrame(EMFUserAdmin userAdmin, PostRegisterStrategy postRegisterStrategy) {
        super("Register New User");

        view = new RegisterUserPanel(userAdmin, postRegisterStrategy, this);

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
