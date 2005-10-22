package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.MyProfileWindow;
import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UpdateUserWindow;
import gov.epa.emissions.framework.client.admin.UserManagerPresenter;
import gov.epa.emissions.framework.client.admin.UserManagerWindow;
import gov.epa.emissions.framework.client.data.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.client.data.DatasetsBrowserWindow;
import gov.epa.emissions.framework.client.data.SectorManagerPresenter;
import gov.epa.emissions.framework.client.data.SectorManagerWindow;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;
import gov.epa.emissions.framework.ui.DefaultWindowLayoutManager;
import gov.epa.emissions.framework.ui.WindowLayoutManager;

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

    private UpdateUserWindow myProfileView;

    private DatasetsBrowserWindow datasetsBrowserView;

    private WindowLayoutManager windowLayoutManager;

    private SectorManagerWindow sectorManagerView;

    // FIXME: where's the associated Presenter ?
    public ManageMenu(EmfSession session, EmfFrame parent, JDesktopPane desktop, MessagePanel messagePanel,
            WindowLayoutManager windowLayoutManager) {
        super("Manage");
        super.setName("manage");

        this.session = session;
        this.desktop = desktop;
        this.parent = parent;
        this.windowLayoutManager = windowLayoutManager;

        super.add(createDatasets(parent, messagePanel));
        super.add(createDisabledMenuItem("Dataset Types"));
        super.add(createSectors(session.getDataServices(), parent, messagePanel));
        super.addSeparator();

        addUsers(session.getUser());
        super.add(createMyProfile(session));
    }

    private JMenuItem createMyProfile(final EmfSession session) {
        JMenuItem menuItem = new JMenuItem("My Profile");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                displayMyProfile(session);
            }
        });

        return menuItem;
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

    private JMenuItem createDatasets(final EmfFrame parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Datasets");
        menuItem.setName("datasets");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displayDatasets(parent);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });

        return menuItem;
    }

    private JMenuItem createSectors(final DataServices dataServices, final EmfFrame parent,
            final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Sectors");
        menuItem.setName("sectors");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displaySectors(dataServices, parent);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });

        return menuItem;
    }

    protected void displaySectors(DataServices dataServices, EmfFrame parent) throws EmfException {
        // FIXME: cull out the pattern - singleton
        if (sectorManagerView != null) {
            sectorManagerView.bringToFront();
            return;
        }

        sectorManagerView = new SectorManagerWindow(parent, desktop);
        windowLayoutManager.add(sectorManagerView);
        desktop.add(sectorManagerView);
        
        SectorManagerPresenter presenter = new SectorManagerPresenter(sectorManagerView, dataServices);
        presenter.doDisplay();
    }

    private void displayDatasets(EmfFrame parent) throws EmfException {
        if (datasetsBrowserView != null) {
            datasetsBrowserView.bringToFront();
            return;
        }

        datasetsBrowserView = new DatasetsBrowserWindow(session, parent, desktop);
        windowLayoutManager.add(datasetsBrowserView);
        desktop.add(datasetsBrowserView);

        WindowLayoutManager browserLayout = new DefaultWindowLayoutManager(datasetsBrowserView);
        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(session.getDataServices(), browserLayout);
        presenter.doDisplay(datasetsBrowserView);
    }

    private void displayMyProfile(EmfSession session) {
        if (myProfileView != null) {
            myProfileView.bringToFront();
            return;
        }

        myProfileView = new MyProfileWindow(session.getUser(), desktop);
        windowLayoutManager.add(myProfileView);
        desktop.add(myProfileView);

        UpdateUserPresenter presenter = new UpdateUserPresenter(session.getUserServices());
        presenter.display(myProfileView);
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

            WindowLayoutManager layoutManager = new DefaultWindowLayoutManager(userManagerView);
            UserManagerPresenter presenter = new UserManagerPresenter(session.getUser(), userServices, layoutManager);
            presenter.display(userManagerView);
        } catch (Exception e) {
            // TODO: error handling
        }
    }

    public void observe(EmfConsolePresenter presenter) {
        this.presenter = presenter;
    }

}
