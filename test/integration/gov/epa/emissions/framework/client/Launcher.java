package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.UserServicesTransport;
import gov.epa.emissions.framework.commons.UserServices;

import javax.swing.JFrame;

public class Launcher {

    public static void main(String[] args) {
        String endpoint = "http://ben.cep.unc.edu:8080/emf/services/gov.epa.emf.UserServices";
        UserServices userAdmin = new UserServicesTransport(endpoint);

        LoginWindow login = new LoginWindow(userAdmin);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(userAdmin, login);
        presenter.observe();

        login.display();
    }

}
