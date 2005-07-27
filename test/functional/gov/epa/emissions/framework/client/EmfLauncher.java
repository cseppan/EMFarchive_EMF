package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import javax.swing.JFrame;

public class EmfLauncher {

    public static void main(String[] args) throws Exception {
        String endpoint = "http://localhost:8080/emf/services/EMFUserManagerService";
        EMFUserAdmin userAdmin = new EMFUserAdminTransport(endpoint);

        LoginWindow login = new LoginWindow(userAdmin);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(userAdmin, login);
        presenter.init();

        login.setVisible(true);
    }

}
