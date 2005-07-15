package gov.epa.emissions.framework.client.security;

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
import javax.swing.JTable;

public class UsersManagementConsole extends JFrame implements UsersManagementView {

    private UsersManagementPresenter presenter;

    public UsersManagementConsole(EMFUserAdmin userAdmin) {
        UsersManagementModel model = new UsersManagementModel(userAdmin.getUsers());

        JPanel layoutPanel = layout(model);

        this.setSize(new Dimension(800, 150));
        this.setLocation(new Point(400, 200));
        this.setTitle("User Management Console");

        this.getContentPane().add(layoutPanel);
    }

    private JPanel layout(UsersManagementModel model) {
        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(600, 70));

        JPanel buttonsPanel = createButtonsPanel();

        JPanel layout = new JPanel();

        layout.setLayout(new BorderLayout());
        layout.add(scrollPane, BorderLayout.CENTER);
        layout.add(buttonsPanel, BorderLayout.SOUTH);

        return layout;
    }

    private JPanel createButtonsPanel() {
        JButton closeButton = new JButton("Close");//TODO: add event handler
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

    public void closeView() {
        super.hide();
    }

}
