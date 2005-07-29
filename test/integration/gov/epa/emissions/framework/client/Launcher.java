package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import javax.swing.JFrame;

public class Launcher {

    public static void main(String[] args) {
        String endpoint = "http://localhost:8080/emf/services/EMFUserManagerService";
        EMFUserAdmin userAdmin = new EMFUserAdminTransport(endpoint);

        LoginWindow login = new LoginWindow(userAdmin);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(userAdmin, login);
        presenter.observe();

        login.display();
    }

}
