package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.login.RegisterUserWindow;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.util.Collections;

import javax.swing.JFrame;

public class RegisterUserWindowLauncher {

    public static void main(String[] args) throws Exception {
        EMFUserAdmin createUserAdmin = new EMFUserAdminStub(Collections.EMPTY_LIST);

        RegisterUserWindow window = new RegisterUserWindow();
        RegisterUserPresenter presenter = new RegisterUserPresenter(createUserAdmin, window);
        presenter.init();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setVisible(true);
    }

}
