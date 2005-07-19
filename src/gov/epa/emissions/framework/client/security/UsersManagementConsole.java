package gov.epa.emissions.framework.client.security;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.transport.EMFUserAdmin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class UsersManagementConsole extends JFrame implements UsersManagementView {

    private UsersManagementPresenter presenter;

    public UsersManagementConsole(EMFUserAdmin userAdmin) {        
        UsersManagementTableModel delegateModel = new UsersManagementTableModel(userAdmin);
        SortFilterSelectModel selectModel = new SortFilterSelectModel(delegateModel);
        
        SortFilterSelectionPanel sortFilterSelectPanel = new SortFilterSelectionPanel(this, selectModel);
        
        JPanel layoutPanel = layout(sortFilterSelectPanel);

        this.setSize(new Dimension(500, 200));
        this.setLocation(new Point(400, 200));
        this.setTitle("User Management Console");

        this.getContentPane().add(layoutPanel);
    }

    private JPanel layout(JPanel sortFilterSelectPanel) {
        JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());

        layout.add(scrollPane, BorderLayout.CENTER);        
        layout.add(createButtonsPanel(), BorderLayout.SOUTH);

        return layout;
    }

    private JPanel createButtonsPanel() {
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(presenter != null) {
                    presenter.notifyCloseView();
                }
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        buttonsPanel.add(closeButton, BorderLayout.EAST);

        return buttonsPanel;
    }

    public void setViewObserver(UsersManagementPresenter presenter) {
        this.presenter = presenter;
    }

    public void close() {
        super.hide();
    }

}
