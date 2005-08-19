package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.EmfConsolePresenter;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import javax.swing.JFrame;

public class ImportWindowLauncher {

    public static void main(String[] args) throws EmfException {
        ServiceLocator serviceLocator = new RemoteServiceLocator("http://localhost:8080/emf/services");

        UserServices userServices = serviceLocator.getUserServices();
        User user = userServices.getUser("emf");

        EmfConsole console = new EmfConsole(user, serviceLocator);
        console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();

        console.display();
    }

}
