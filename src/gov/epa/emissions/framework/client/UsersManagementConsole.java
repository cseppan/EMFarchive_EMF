package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.EMFUserAdmin;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class UsersManagementConsole extends JPanel {

    private EMFUserAdmin userAdmin;

    public UsersManagementConsole(EMFUserAdmin userAdmin) {
        this.userAdmin = userAdmin;
        UsersManagementModel model = new UsersManagementModel(userAdmin.getUsers());

        layout(model);
    }

    private void layout(UsersManagementModel model) {
        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);        
        table.setPreferredScrollableViewportSize(new Dimension(600, 70));

        JPanel buttonsPanel = createButtonsPanel();
        
        this.setLayout(new BorderLayout());        
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.SOUTH);
    }

    private JPanel createButtonsPanel() {        
        JButton closeButton = new JButton("Close");//TODO: add event handler
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        buttonsPanel.add(closeButton, BorderLayout.EAST);
       
        return buttonsPanel;
    }

}
