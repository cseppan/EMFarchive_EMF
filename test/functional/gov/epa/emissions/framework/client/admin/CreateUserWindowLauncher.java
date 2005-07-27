package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.login.CreateUserWindow;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;

import javax.swing.JFrame;

public class CreateUserWindowLauncher {

    public static void main(String[] args) throws Exception {
        EMFUserAdmin createUserAdmin = new EMFUserAdminStub(Collections.EMPTY_LIST);

        CreateUserWindow window = new CreateUserWindow();
        CreateUserPresenter presenter = new CreateUserPresenter(createUserAdmin, window);
        presenter.init();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setVisible(true);
    }

}
