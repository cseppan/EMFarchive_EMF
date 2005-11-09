package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.console.EmfConsolePresenter;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import javax.swing.JFrame;

public class ImportWindowLauncher {

    public static void main(String[] args) throws Exception {
        ServiceLocator serviceLocator = new RemoteServiceLocator("http://localhost:8080/emf/services");

        UserServices userServices = serviceLocator.getUserServices();
        User user = userServices.getUser("emf");

        EmfConsole view = new EmfConsole(new DefaultEmfSession(user, serviceLocator));
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EmfConsolePresenter presenter = new EmfConsolePresenter();
        presenter.display(view);
    }

}
