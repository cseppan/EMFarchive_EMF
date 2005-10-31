package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.MyProfileWindow;
import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UsersManagerPresenter;
import gov.epa.emissions.framework.client.admin.UsersManager;
import gov.epa.emissions.framework.client.data.DatasetTypesManagerPresenter;
import gov.epa.emissions.framework.client.data.DatasetTypesManagerWindow;
import gov.epa.emissions.framework.client.data.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.client.data.DatasetsBrowserWindow;
import gov.epa.emissions.framework.client.data.SectorsManagerPresenter;
import gov.epa.emissions.framework.client.data.SectorsManagerWindow;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;
import gov.epa.emissions.framework.ui.DefaultViewLayout;
import gov.epa.emissions.framework.ui.ViewLayout;

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

    private ViewLayout viewLayout;

    // FIXME: where's the associated Presenter ?
    public ManageMenu(EmfSession session, EmfFrame parent, JDesktopPane desktop, MessagePanel messagePanel,
            ViewLayout windowLayoutManager) {
        super("Manage");
        super.setName("manage");

        this.session = session;
        this.desktop = desktop;
        this.parent = parent;
        this.viewLayout = windowLayoutManager;

        super.add(createDatasets(parent, messagePanel));
        super.add(createDatasetTypes(session.getDatasetTypesServices(), parent, messagePanel));
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

    // FIXME: each of the menu-item and it's handles are similar. Refactor ?
    private JMenuItem createDatasetTypes(final DatasetTypesServices services, final EmfFrame parent,
            final MessagePanel messagePanel) {
        JMenuItem menuItem = new JMenuItem("Dataset Types");
        menuItem.setName("datasetTypes");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displayDatasetTypes(services, parent);
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

    protected void displayDatasetTypes(DatasetTypesServices services, EmfFrame parent) throws EmfException {
        if (viewLayout.activate("DatasetTypes Manager"))
            return;

        DatasetTypesManagerWindow view = new DatasetTypesManagerWindow(parent, desktop);
        viewLayout.add(view, "DatasetTypes Manager");
        desktop.add(view);

        ViewLayout viewLayout = new DefaultViewLayout(view);
        DatasetTypesManagerPresenter presenter = new DatasetTypesManagerPresenter(view, services, viewLayout);
        presenter.doDisplay();
    }

    protected void displaySectors(DataServices dataServices, EmfFrame parent) throws EmfException {
        if (viewLayout.activate("Sectors Manager"))
            return;

        SectorsManagerWindow view = new SectorsManagerWindow(parent, desktop);
        viewLayout.add(view, "Sectors Manager");
        desktop.add(view);

        ViewLayout sectorsLayout = new DefaultViewLayout(view);
        SectorsManagerPresenter presenter = new SectorsManagerPresenter(view, dataServices, sectorsLayout);
        presenter.doDisplay();
    }

    private void displayDatasets(EmfFrame parent) throws EmfException {
        if (viewLayout.activate("Datasets Browser"))
            return;

        DatasetsBrowserWindow datasetsBrowserView = new DatasetsBrowserWindow(session, parent, desktop);
        viewLayout.add(datasetsBrowserView, "Datasets Browser");
        desktop.add(datasetsBrowserView);

        ViewLayout browserLayout = new DefaultViewLayout(datasetsBrowserView);
        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(session.getDataServices(), browserLayout);
        presenter.doDisplay(datasetsBrowserView);
    }

    private void displayMyProfile(EmfSession session) {
        if (viewLayout.activate("My Profile"))
            return;

        MyProfileWindow myProfileView = new MyProfileWindow(session.getUser(), desktop);
        viewLayout.add(myProfileView, "My Profile");
        desktop.add(myProfileView);

        UpdateUserPresenter presenter = new UpdateUserPresenter(session.getUserServices());
        presenter.display(myProfileView);
    }

    public void displayUserManager() {
        if (viewLayout.activate("Users Manager"))
            return;

        try {
            UserServices userServices = session.getUserServices();

            UsersManager usesrManagerView = new UsersManager(session.getUser(), userServices, parent, desktop);
            viewLayout.add(usesrManagerView, "Users Manager");
            desktop.add(usesrManagerView);

            ViewLayout userManagerViewLayout = new DefaultViewLayout(usesrManagerView);
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
