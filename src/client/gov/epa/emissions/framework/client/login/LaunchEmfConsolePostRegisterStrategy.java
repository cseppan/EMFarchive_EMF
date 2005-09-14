package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.EmfConsolePresenter;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.admin.PostRegisterStrategy;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.User;

public class LaunchEmfConsolePostRegisterStrategy implements PostRegisterStrategy {

    private ServiceLocator serviceLocator;

    public LaunchEmfConsolePostRegisterStrategy(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public void execute(User user) {
        EmfConsole console = new EmfConsole(new EmfSession(user, serviceLocator));

        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();

        console.display();
    }

}
