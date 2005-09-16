package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.ReusableInteralFrame;

public class RegisterUserInternalFrame extends ReusableInteralFrame {

    private RegisterUserPanel view;

    public RegisterUserInternalFrame(PostRegisterStrategy postRegisterStrategy) {
        super("Register New User");

        view = new RegisterUserPanel(postRegisterStrategy, new CloseViewOnCancelStrategy(), this, new AddAdminOption());

        super.setSize(view.getSize());
        super.getContentPane().add(view);
    }

    public RegisterUserView getView() {
        return view;
    }

}
