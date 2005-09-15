package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.EmfWidgetContainer;

public class RegisterUserInternalFrame extends EmfInteralFrame implements EmfWidgetContainer {

    private RegisterUserPanel view;

    public RegisterUserInternalFrame(PostRegisterStrategy postRegisterStrategy) {
        super("Register New User");

        view = new RegisterUserPanel(postRegisterStrategy, new CloseViewOnCancelStrategy(), this, new AddAdminOption());

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
