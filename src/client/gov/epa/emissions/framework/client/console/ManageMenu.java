package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
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
import gov.epa.emissions.framework.ui.CascadeLayout;
import gov.epa.emissions.framework.ui.ViewLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ManageMenu extends JMenu {

    private EmfConsolePresenter presenter;

    private EmfSession session;

    private EmfConsole parent;

    private ViewLayout viewLayout;

    // FIXME: where's the associated Presenter ?
    public ManageMenu(EmfSession session, EmfConsole parent, MessagePanel messagePanel, ViewLayout viewLayout) {
        super("Manage");
        super.setName("manage");

        this.session = session;
        this.parent = parent;
        this.viewLayout = viewLayout;

        super.add(createDatasets(parent, messagePanel));
        super.add(createDatasetTypes(parent, messagePanel));
        super.add(createSectors(session.dataCommonsService(), parent, messagePanel));
        super.addSeparator();

        addUsers(session.user());
        super.add(createMyProfile(session, messagePanel));
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
        if (viewLayout.activate("DatasetTypes Manager"))
            return;

        DatasetTypesManagerWindow view = new DatasetTypesManagerWindow(parent);
        viewLayout.add(view, "DatasetTypes Manager");
        parent.addToDesktop(view);

        ViewLayout viewLayout = new CascadeLayout(view);
        DatasetTypesManagerPresenter presenter = new DatasetTypesManagerPresenter(session, view, viewLayout);
        presenter.doDisplay();
    }

    protected void displaySectors(DataCommonsService service, EmfConsole parent) throws EmfException {
        if (viewLayout.activate("Sectors Manager"))
            return;

        SectorsManagerWindow view = new SectorsManagerWindow(parent);
        viewLayout.add(view, "Sectors Manager");
        parent.addToDesktop(view);

        ViewLayout sectorsLayout = new CascadeLayout(view);
        SectorsManagerPresenter presenter = new SectorsManagerPresenter(session, view, service, sectorsLayout);
        presenter.doDisplay();
    }

    private void displayDatasets(EmfConsole parent) throws EmfException {
        if (viewLayout.activate("Datasets Browser"))
            return;

        DatasetsBrowserWindow datasetsBrowserView = new DatasetsBrowserWindow(session, parent);
        viewLayout.add(datasetsBrowserView, "Datasets Browser");
        parent.addToDesktop(datasetsBrowserView);

        ViewLayout browserLayout = new CascadeLayout(datasetsBrowserView);
        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(session.serviceLocator(), browserLayout);
        presenter.doDisplay(datasetsBrowserView);
    }

    private void displayMyProfile(EmfSession session, MessagePanel messagePanel) {
        if (viewLayout.activate("Update - My Profile") || viewLayout.activate("View - My Profile"))
            return;

        UpdateMyProfileWindow updatable = new UpdateMyProfileWindow(session.user(), parent.desktop());
        viewLayout.add(updatable, "Update - My Profile");
        parent.addToDesktop(updatable);

        ViewMyProfileWindow viewable = new ViewMyProfileWindow(parent.desktop());
        viewLayout.add(viewable, "View - My Profile");
        parent.addToDesktop(viewable);

        UpdateUserPresenter presenter = new UpdateUserPresenterImpl(session, session.user(), session.userService());
        try {
            presenter.display(updatable, viewable);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void displayUserManager() {
        if (viewLayout.activate("Users Manager"))
            return;

        UserService userServices = session.userService();

        UsersManager usesrManagerView = new UsersManager(session, userServices, parent);
        viewLayout.add(usesrManagerView, "Users Manager");
        parent.addToDesktop(usesrManagerView);

        ViewLayout userManagerViewLayout = new CascadeLayout(usesrManagerView);
        UsersManagerPresenter presenter = new UsersManagerPresenter(session, userServices, userManagerViewLayout);
        presenter.display(usesrManagerView);
    }

    public void observe(EmfConsolePresenter presenter) {
        this.presenter = presenter;
    }

}
