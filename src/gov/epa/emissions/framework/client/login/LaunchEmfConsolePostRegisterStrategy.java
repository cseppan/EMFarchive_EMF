package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.admin.PostRegisterStrategy;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

public class LaunchEmfConsolePostRegisterStrategy implements PostRegisterStrategy {

    private EMFUserAdmin userAdmin;

    public LaunchEmfConsolePostRegisterStrategy(EMFUserAdmin admin) {
        userAdmin = admin;
    }

    public void execute(User user) {
        EmfConsole console = new EmfConsole(user, userAdmin);
        console.setVisible(true);
    }

}
