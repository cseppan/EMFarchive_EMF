package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;

import javax.swing.JDesktopPane;

public class RegisterUserInternalFrame extends ReusableInteralFrame implements RegisterUserDesktopView {

    private RegisterUserPanel view;

    public RegisterUserInternalFrame(PostRegisterStrategy postRegisterStrategy, JDesktopPane desktop,
            DesktopManager desktopManager) {
        super("Register New User", desktop, desktopManager);
        
        view = new RegisterUserPanel(postRegisterStrategy, new CloseViewOnCancelStrategy(), this, new AddAdminOption());

        super.dimensions(view.getSize());
        super.getContentPane().add(view);
    }

    public void observe(RegisterUserPresenter presenter) {
        view.observe(presenter);
    }

}
