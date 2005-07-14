package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.EMFUserAdmin;

import javax.swing.JPanel;
import javax.swing.JTable;

public class UserManagementConsole extends JPanel {

    private EMFUserAdmin userAdmin;

    public UserManagementConsole(EMFUserAdmin userAdmin) {
        this.userAdmin = userAdmin;
                
        UsersManagementModel model = new UsersManagementModel(userAdmin.getUsers());
        JTable table = new JTable(model);
        
        this.add(table);
    }

}
