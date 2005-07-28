package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

public class LaunchEmfConsolePostRegisterStrategy implements PostRegisterStrategy {

    private EMFUserAdmin userAdmin;

    public LaunchEmfConsolePostRegisterStrategy(EMFUserAdmin admin) {
        userAdmin = admin;
    }

    public void execute() {
        EmfConsole console = new EmfConsole(userAdmin);
        console.setVisible(true);
    }

}
