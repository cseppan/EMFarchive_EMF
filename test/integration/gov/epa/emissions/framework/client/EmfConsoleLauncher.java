package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import javax.swing.JFrame;

public class EmfConsoleLauncher {

    public static void main(String[] args) throws EmfException {
        ServiceLocator serviceLocator = new RemoteServiceLocator("http://localhost:8080/emf/services/");

        UserServices userAdmin = serviceLocator.getUserServices();
        User user = userAdmin.getUser("admin");

        EmfConsole console = new EmfConsole(new EmfSession(user, serviceLocator));
        console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();

        console.setVisible(true);
    }

}
