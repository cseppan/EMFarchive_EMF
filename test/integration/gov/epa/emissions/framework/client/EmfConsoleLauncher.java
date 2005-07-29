package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import javax.swing.JFrame;

public class EmfConsoleLauncher {

    public static void main(String[] args) throws EmfException {
        String endpoint = "http://localhost:8080/emf/services/EMFUserManagerService";
        EMFUserAdmin userAdmin = new EMFUserAdminTransport(endpoint);
        
        User user = userAdmin.getUser("admin");
        
        EmfConsole console = new EmfConsole(user, userAdmin);
        console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();
        
        console.setVisible(true);
    }

}
