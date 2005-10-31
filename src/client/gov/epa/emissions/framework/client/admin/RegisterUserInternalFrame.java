package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.ReusableInteralFrame;

import javax.swing.JDesktopPane;

public class RegisterUserInternalFrame extends ReusableInteralFrame implements RegisterUserDesktopView {

    private RegisterUserPanel view;

    public RegisterUserInternalFrame(PostRegisterStrategy postRegisterStrategy, JDesktopPane desktop) {
        super("Register New User", desktop);

        view = new RegisterUserPanel(postRegisterStrategy, new CloseViewOnCancelStrategy(), this, new AddAdminOption());

        super.setSize(view.getSize());
        super.getContentPane().add(view);
    }

    public void observe(RegisterUserPresenter presenter) {
        view.observe(presenter);
    }

}
