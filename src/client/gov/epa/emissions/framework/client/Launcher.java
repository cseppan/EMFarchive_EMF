package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import javax.swing.JFrame;

public class Launcher {

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:8080/emf/services";// default
        if (args.length == 1)
            url = args[0];

        ServiceLocator serviceLocator = new RemoteServiceLocator(url);

        LoginWindow view = new LoginWindow(serviceLocator);
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(serviceLocator.userService());
        presenter.display(view);
    }

}
