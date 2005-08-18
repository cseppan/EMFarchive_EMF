package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import javax.swing.JFrame;

public class Launcher {

    public static void main(String[] args) {
        ServiceLocator serviceLocator = new RemoteServiceLocator("http://ben.cep.unc.edu:8080/emf/services");

        LoginWindow login = new LoginWindow(serviceLocator);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(serviceLocator.getUserServices(), login);
        presenter.observe();

        login.display();
    }

}
