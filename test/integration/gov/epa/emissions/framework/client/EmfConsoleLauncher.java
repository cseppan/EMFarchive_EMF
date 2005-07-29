package gov.epa.emissions.framework.client;

import javax.swing.JFrame;

import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

public class EmfConsoleLauncher {

    public static void main(String[] args) {
        String endpoint = "http://localhost:8080/emf/services/EMFUserManagerService";
        EMFUserAdmin userAdmin = new EMFUserAdminTransport(endpoint);

        EmfConsole console = new EmfConsole(userAdmin);
        console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        console.setVisible(true);
    }

}
