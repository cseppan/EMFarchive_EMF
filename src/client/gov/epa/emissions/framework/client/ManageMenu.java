package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UpdateUserWindow;
import gov.epa.emissions.framework.client.admin.UserManagerPresenter;
import gov.epa.emissions.framework.client.admin.UserManagerWindow;
import gov.epa.emissions.framework.client.exim.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.client.exim.DatasetsBrowserWindow;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ManageMenu extends JMenu {

    private EmfConsolePresenter presenter;

    private JDesktopPane desktop;

    private EmfSession session;

    private EmfFrame parent;

    public ManageMenu(EmfSession session, EmfFrame parent, JDesktopPane desktop, MessagePanel messagePanel) {
        super("Manage");
        this.session = session;
        this.desktop = desktop;
        this.parent = parent;

        addDatasets(parent, messagePanel);

        super.add(createDisabledMenuItem("Dataset Types"));
        super.add(createDisabledMenuItem("Sectors"));
        super.addSeparator();

        addUsers(session.getUser());
        addMyProfile();
    }

    private void addMyProfile() {
        JMenuItem myProfile = new JMenuItem("My Profile");
        myProfile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                displayUpdateUser();
            }
        });
        super.add(myProfile);
    }

    private void addUsers(User user) {
        if (user.isInAdminGroup()) {
            JMenuItem users = new JMenuItem("Users");
            users.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (presenter != null)
                        presenter.notifyManageUsers();
                }
            });
            super.add(users);
        }
    }

    private void addDatasets(final EmfFrame parent, final MessagePanel messagePanel) {
        JMenuItem datasets = new JMenuItem("Datasets");
        datasets.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displayDatasets(parent);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        super.add(datasets);
    }

    private void displayDatasets(EmfFrame parent) throws EmfException {
        DatasetsBrowserWindow view = new DatasetsBrowserWindow(session.getDataServices(), parent);
        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(session);
        presenter.observe(view);

        desktop.add(view);

        view.display();
    }

    private void displayUpdateUser() {
        UpdateUserWindow view = new UpdateUserWindow(session.getUser());
        UpdateUserPresenter presenter = new UpdateUserPresenter(session.getUserServices(), view);
        presenter.observe();

        desktop.add(view);

        view.display();
    }

    private JMenuItem createDisabledMenuItem(String name) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setEnabled(false);

        return menuItem;
    }

    public void displayUserManager() {
        try {
            UserServices userServices = session.getUserServices();
            User user = session.getUser();
            UserManagerWindow console = new UserManagerWindow(user, userServices, parent);
            UserManagerPresenter presenter = new UserManagerPresenter(user, userServices, console);
            presenter.observe();

            desktop.add(console);
            console.display();
        } catch (Exception e) {
            // TODO: error handling
        }
    }

    public void observe(EmfConsolePresenter presenter) {
        this.presenter = presenter;
    }

}
