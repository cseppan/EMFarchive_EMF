package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.EMFUserAdmin;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class UsersManagementConsole extends JPanel {

    private EMFUserAdmin userAdmin;

    public UsersManagementConsole(EMFUserAdmin userAdmin) {
        this.userAdmin = userAdmin;
                
        UsersManagementModel model = new UsersManagementModel(userAdmin.getUsers());
        JTable table = new JTable(model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(600, 70));
        
        this.add(scrollPane);
    }

}
