package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.admin.UpdateMyProfileWindow;
import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UpdateUserPresenterImpl;
import gov.epa.emissions.framework.client.admin.UsersManager;
import gov.epa.emissions.framework.client.admin.UsersManagerPresenter;
import gov.epa.emissions.framework.client.admin.ViewMyProfileWindow;
import gov.epa.emissions.framework.client.data.DatasetTypesManagerPresenter;
import gov.epa.emissions.framework.client.data.DatasetTypesManagerWindow;
import gov.epa.emissions.framework.client.data.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.client.data.DatasetsBrowserWindow;
import gov.epa.emissions.framework.client.data.SectorsManagerPresenter;
import gov.epa.emissions.framework.client.data.SectorsManagerWindow;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ManageMenu extends JMenu {

    private EmfConsolePresenter presenter;

    private EmfSession session;

    private EmfConsole parent;

    private DesktopManager desktopManager;

    // FIXME: where's the associated Presenter ?
    public ManageMenu(EmfSession session, EmfConsole parent, MessagePanel messagePanel) {
        super("Manage");
        super.setName("manage");

        this.session = session;
        this.parent = parent;

        super.add(createDatasets(parent, messagePanel));
        super.add(createDatasetTypes(parent, messagePanel));
        super.add(createSectors(session.dataCommonsService(), parent, messagePanel));
        super.addSeparator();

        manageUsers(session.user(), messagePanel);
        super.add(createMyProfile(session, messagePanel));
    }

    public ManageMenu(EmfSession session, EmfConsole parent, MessagePanel messagePanel, DesktopManager desktopManager) {
        this(session, parent, messagePanel);
        this.desktopManager = desktopManager;
    }

    private JMenuItem createMyProfile(final EmfSession session, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("My Profile");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                displayMyProfile(session, messagePanel);
            }
        });

        return menuItem;
    }

    private void manageUsers(User user, final MessagePanel messagePanel) {
        if (user.isAdmin()) {
            JMenuItem users = new JMenuItem("Users");
            users.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        presenter.notifyManageUsers();
                    } catch (EmfException e) {
                        messagePanel.setError(e.getMessage());
                    }
                }
            });

            super.add(users);
        }
    }

    private JMenuItem createDatasets(final EmfConsole parent, final MessagePanel messagePanel) {
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

    // FIXME: each of the menu-item and it's handles are similar. Refactor ?
    private JMenuItem createDatasetTypes(final EmfConsole parent, final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Dataset Types");
        menuItem.setName("datasetTypes");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displayDatasetTypes(parent);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });

        return menuItem;
    }

    private JMenuItem createSectors(final DataCommonsService service, final EmfConsole parent,
            final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Sectors");
        menuItem.setName("sectors");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displaySectors(service, parent);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });

        return menuItem;
    }

    protected void displayDatasetTypes(EmfConsole parent) throws EmfException {
        DatasetTypesManagerWindow view = new DatasetTypesManagerWindow(parent, desktopManager);
        parent.addToDesktop(view);

        DatasetTypesManagerPresenter presenter = new DatasetTypesManagerPresenter(session, view);
        presenter.doDisplay();
    }

    protected void displaySectors(DataCommonsService service, EmfConsole parent) throws EmfException {
        SectorsManagerWindow view = new SectorsManagerWindow(parent, desktopManager);
        parent.addToDesktop(view);

        SectorsManagerPresenter presenter = new SectorsManagerPresenter(session, view, service);
        presenter.doDisplay();
    }

    private void displayDatasets(EmfConsole parent) throws EmfException {
        DatasetsBrowserWindow datasetsBrowserView = new DatasetsBrowserWindow(session, parent, desktopManager);
        parent.addToDesktop(datasetsBrowserView);

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(session);
        presenter.doDisplay(datasetsBrowserView);
    }

    private void displayMyProfile(EmfSession session, MessagePanel messagePanel) {
        UpdateMyProfileWindow updatable = new UpdateMyProfileWindow(parent.desktop(), desktopManager);
        parent.addToDesktop(updatable);

        ViewMyProfileWindow viewable = new ViewMyProfileWindow(parent.desktop(), desktopManager);
        parent.addToDesktop(viewable);

        UpdateUserPresenter presenter = new UpdateUserPresenterImpl(session, session.user(), session.userService());
        try {
            presenter.display(updatable, viewable);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void displayUserManager() throws EmfException {
        UserService userService = session.userService();

        UsersManager usesrManagerView = new UsersManager(session, parent, desktopManager);
        parent.addToDesktop(usesrManagerView);

        UsersManagerPresenter presenter = new UsersManagerPresenter(session, userService);
        presenter.display(usesrManagerView);
    }

    public void observe(EmfConsolePresenter presenter) {
        this.presenter = presenter;
    }

}
