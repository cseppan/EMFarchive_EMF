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

    private UserManagerWindow userManagerView;

    private UpdateUserWindow updateUserView;

    private DatasetsBrowserWindow datasetsBrowserView;

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
        addMyProfile(session);
    }

    private void addMyProfile(final EmfSession session) {
        JMenuItem myProfile = new JMenuItem("My Profile");
        myProfile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                displayUpdateUser(session);
            }
        });
        super.add(myProfile);
    }

    private void addUsers(User user) {
        if (user.isInAdminGroup()) {
            JMenuItem users = new JMenuItem("Users");
            users.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
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
        if (datasetsBrowserView != null) {
            datasetsBrowserView.bringToFront();
            return;
        }

        datasetsBrowserView = new DatasetsBrowserWindow(session.getDataServices(), parent, desktop);
        desktop.add(datasetsBrowserView);

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(session);
        presenter.observe(datasetsBrowserView);

        datasetsBrowserView.display();
    }

    private void displayUpdateUser(EmfSession session) {
        if (updateUserView != null) {
            updateUserView.bringToFront();
            return;
        }

        updateUserView = new UpdateUserWindow(session.getUser(), desktop);
        desktop.add(updateUserView);

        UpdateUserPresenter presenter = new UpdateUserPresenter(session.getUserServices(), updateUserView);
        presenter.observe();

        updateUserView.display();
    }

    private JMenuItem createDisabledMenuItem(String name) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setEnabled(false);

        return menuItem;
    }

    public void displayUserManager() {
        if (userManagerView != null) {
            userManagerView.bringToFront();
            return;
        }

        try {
            UserServices userServices = session.getUserServices();
            userManagerView = new UserManagerWindow(session.getUser(), userServices, parent, desktop);
            desktop.add(userManagerView);

            UserManagerPresenter presenter = new UserManagerPresenter(session.getUser(), userServices, userManagerView);
            presenter.observe();

            userManagerView.display();
        } catch (Exception e) {
            // TODO: error handling
        }
    }

    public void observe(EmfConsolePresenter presenter) {
        this.presenter = presenter;
    }

}
