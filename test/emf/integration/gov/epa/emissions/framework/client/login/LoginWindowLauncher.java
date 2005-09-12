package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.transport.ServiceLocator;

import org.jmock.Mock;

public class LoginWindowLauncher {

    public static void main(String[] args) {
        Mock serviceLocator = new Mock(ServiceLocator.class);

        LoginWindow view = new LoginWindow((ServiceLocator) serviceLocator.proxy());

        view.display();
    }
}
