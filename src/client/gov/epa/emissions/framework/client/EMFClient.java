package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import javax.swing.JFrame;

public class EMFClient {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equalsIgnoreCase("Help")) {
            displayHelp();
            return;
        }

        run(args);
    }

    private static void displayHelp() {
        System.out
                .println("Usage\njava "
                        + EMFClient.class.getName()
                        + " [url]"
                        + "\n\turl - location of EMF Services. Defaults to "
                        + DEFAULT_URL
                        + "\n\tspecify '-DUSER_PREFERENCES=<full path to EMFPrefs.txt>' to override location of User Preferences");
    }

    private static void run(String[] args) throws Exception {
        String url = DEFAULT_URL;
        if (args.length == 1)
            url = args[0];

        ServiceLocator serviceLocator = new RemoteServiceLocator(url);

        LoginWindow view = new LoginWindow(serviceLocator);
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(serviceLocator.userService());
        presenter.display(view);
    }

}
