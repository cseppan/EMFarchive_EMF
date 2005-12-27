package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.admin.MyProfileWindow;
import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UsersManager;
import gov.epa.emissions.framework.client.admin.UsersManagerPresenter;
import gov.epa.emissions.framework.client.data.DatasetTypesManagerPresenter;
import gov.epa.emissions.framework.client.data.DatasetTypesManagerWindow;
import gov.epa.emissions.framework.client.data.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.client.data.DatasetsBrowserWindow;
import gov.epa.emissions.framework.client.data.SectorsManagerPresenter;
import gov.epa.emissions.framework.client.data.SectorsManagerWindow;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
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
        super.add(createDatasetTypes(session.serviceLocator(), parent, messagePanel));
        super.add(createSectors(session.dataCommonsService(), parent, messagePanel));
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
    private JMenuItem createDatasetTypes(final ServiceLocator serviceLocator, final EmfConsole parent,
            final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Dataset Types");
        menuItem.setName("datasetTypes");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displayDatasetTypes(serviceLocator, parent);
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

    protected void displayDatasetTypes(ServiceLocator serviceLocator, EmfConsole parent) throws EmfException {
        if (viewLayout.activate("DatasetTypes Manager"))
            return;

        DatasetTypesManagerWindow view = new DatasetTypesManagerWindow(parent);
        viewLayout.add(view, "DatasetTypes Manager");
        parent.addToDesktop(view);

        ViewLayout viewLayout = new CascadeLayout(view);
        DatasetTypesManagerPresenter presenter = new DatasetTypesManagerPresenter(view, serviceLocator, viewLayout);
        presenter.doDisplay();
    }

    protected void displaySectors(DataCommonsService service, EmfConsole parent) throws EmfException {
        if (viewLayout.activate("Sectors Manager"))
            return;

        SectorsManagerWindow view = new SectorsManagerWindow(parent);
        viewLayout.add(view, "Sectors Manager");
        parent.addToDesktop(view);

        ViewLayout sectorsLayout = new CascadeLayout(view);
        SectorsManagerPresenter presenter = new SectorsManagerPresenter(view, service, sectorsLayout);
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

    private void displayMyProfile(EmfSession session) {
        if (viewLayout.activate("My Profile"))
            return;

        MyProfileWindow myProfileView = new MyProfileWindow(session.getUser(), parent.desktop());
        viewLayout.add(myProfileView, "My Profile");
        parent.addToDesktop(myProfileView);

        UpdateUserPresenter presenter = new UpdateUserPresenter(session.userService());
        presenter.display(myProfileView);
    }

    public void displayUserManager() {
        if (viewLayout.activate("Users Manager"))
            return;

        try {
            UserService userServices = session.userService();

            UsersManager usesrManagerView = new UsersManager(session.getUser(), userServices, parent);
            viewLayout.add(usesrManagerView, "Users Manager");
            parent.addToDesktop(usesrManagerView);

            ViewLayout userManagerViewLayout = new CascadeLayout(usesrManagerView);
            UsersManagerPresenter presenter = new UsersManagerPresenter(session.getUser(), userServices,
                    userManagerViewLayout);
            presenter.display(usesrManagerView);
        } catch (Exception e) {
            // TODO: error handling
        }
    }

    public void observe(EmfConsolePresenter presenter) {
        this.presenter = presenter;
    }

}
