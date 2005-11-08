package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import javax.swing.JFrame;

public class Launcher {

    public static void main(String[] args) {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = new RemoteServiceLocator("http://localhost:8080/emf/services");
        } catch (Exception e) {
            e.printStackTrace();
        }

        LoginWindow view = new LoginWindow(serviceLocator);
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(serviceLocator.getUserServices());
        presenter.display(view);
    }

}
