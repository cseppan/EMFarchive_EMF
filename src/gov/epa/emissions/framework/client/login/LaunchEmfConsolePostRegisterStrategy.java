package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.EmfConsolePresenter;
import gov.epa.emissions.framework.client.admin.PostRegisterStrategy;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public class LaunchEmfConsolePostRegisterStrategy implements PostRegisterStrategy {

    private UserServices userAdmin;

    public LaunchEmfConsolePostRegisterStrategy(UserServices admin) {
        userAdmin = admin;
    }

    public void execute(User user) {
        EmfConsole console = new EmfConsole(user, userAdmin);

        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();

        console.display();
    }

}
