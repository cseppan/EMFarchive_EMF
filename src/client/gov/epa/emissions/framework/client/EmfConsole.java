package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UpdateUserWindow;
import gov.epa.emissions.framework.client.admin.UserManagerPresenter;
import gov.epa.emissions.framework.client.admin.UserManagerWindow;
import gov.epa.emissions.framework.client.exim.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.client.exim.DatasetsBrowserWindow;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.status.StatusWindow;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

//FIXME: split this class up into smaller ones...getting too big
public class EmfConsole extends EmfFrame implements EmfConsoleView {

    private JDesktopPane desktop;

    private User user;

    private EmfConsolePresenter presenter;

    private ServiceLocator serviceLocator;

    private MessagePanel messagePanel;

    private StatusWindow status;

    private EmfSession session;

    private WindowMenuPresenter windowMenuPresenter;

    // TODO: split the login & logout menu/actions in a separate class ??
    public EmfConsole(EmfSession session) {
        super("EMF Console", "Emissions Modeling Framework (EMF)");
        this.session = session;
        this.user = session.getUser();
        this.serviceLocator = session.getServiceLocator();

        setProperties();
        setLayout();
        showStatus();
    }

    private void setLayout() {
        JMenuBar menuBar = createMenuBar();
        super.setJMenuBar(menuBar);

        messagePanel = new SingleLineMessagePanel();
        menuBar.add(messagePanel);

        desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        this.setContentPane(desktop);
    }

    private void showStatus() {
        StatusServices statusServices = serviceLocator.getStatusServices();
        status = new StatusWindow(user, statusServices, this);
        windowMenuPresenter.notifyAdd(status);
        
        desktop.add(status);

        status.display();
    }

    private void setProperties() {
        super.setSize(new Dimension(900, 700));
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menubar = new JMenuBar();

        menubar.add(createFileMenu());
        menubar.add(createManageMenu());
        menubar.add(createWindowMenu());
        menubar.add(createHelpMenu());

        return menubar;
    }

    private WindowMenu createWindowMenu() {
        WindowMenu menu = new WindowMenu();
        windowMenuPresenter = new WindowMenuPresenter(menu);

        return menu;
    }

    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        menu.setName("file");

        JMenuItem importMenu = new JMenuItem("Import");
        importMenu.setName("import");
        importMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displayImport();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        menu.add(importMenu);

        menu.addSeparator();

        JMenuItem logout = new JMenuItem("Logout");
        logout.setName("logout");
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout();
            }
        });
        menu.add(logout);

        JMenuItem exit = new JMenuItem("Exit");
        exit.setName("exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // TODO: logout before exiting. Should prompt the user ?
                System.exit(0);
            }
        });
        menu.add(exit);

        return menu;
    }

    protected void displayImport() throws EmfException {
        ExImServices eximServices = serviceLocator.getExImServices();
        ImportWindow view = new ImportWindow(eximServices);
        ImportPresenter presenter = new ImportPresenter(user, eximServices);
        presenter.observe(view);

        desktop.add(view);
    }

    private void logout() {
        UserServices userServices = serviceLocator.getUserServices();
        LoginWindow view = new LoginWindow(serviceLocator);
        LoginPresenter presenter = new LoginPresenter(userServices, view);
        presenter.observe();

        view.display();

        close();
    }

    private void close() {
        // TODO: auto logout of a session
        status.close();
        super.dispose();
    }

    private JMenuItem createDisabledMenuItem(String name) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setEnabled(false);

        return menuItem;
    }

    private JMenu createManageMenu() {
        JMenu menu = new JMenu("Manage");

        JMenuItem datasets = new JMenuItem("Datasets");
        datasets.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displayDatasets();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        menu.add(datasets);

        menu.add(createDisabledMenuItem("Dataset Types"));
        menu.add(createDisabledMenuItem("Sectors"));
        menu.addSeparator();

        if (user.isInAdminGroup()) {
            JMenuItem users = new JMenuItem("Users");
            users.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (presenter != null)
                        presenter.notifyManageUsers();
                }
            });
            menu.add(users);
        }

        JMenuItem myProfile = new JMenuItem("My Profile");
        myProfile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                displayUpdateUser();
            }
        });
        menu.add(myProfile);

        return menu;
    }

    private void displayDatasets() throws EmfException {
        DatasetsBrowserWindow view = new DatasetsBrowserWindow(serviceLocator.getDataServices(), this);
        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(session);
        presenter.observe(view);

        desktop.add(view);

        view.display();
    }

    private void displayUpdateUser() {
        UpdateUserWindow view = new UpdateUserWindow(user);
        UpdateUserPresenter presenter = new UpdateUserPresenter(serviceLocator.getUserServices(), view);
        presenter.observe();

        desktop.add(view);

        view.display();
    }

    public void displayUserManager() {
        try {
            UserServices userServices = serviceLocator.getUserServices();
            UserManagerWindow console = new UserManagerWindow(user, userServices, this);
            UserManagerPresenter presenter = new UserManagerPresenter(user, userServices, console);
            presenter.observe();

            desktop.add(console);
            console.display();
        } catch (Exception e) {
            // TODO: error handling
        }
    }

    private JMenu createHelpMenu() {
        JMenu menu = new JMenu("Help");

        menu.add(createDisabledMenuItem("User Guide"));
        menu.add(createDisabledMenuItem("Documentation"));

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showMessageDialog(EmfConsole.this,
                        "\nEmissions Modeling Framework (EMF)\nVersion: pre-alpha", "EMF",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(about);

        return menu;
    }

    public void setObserver(EmfConsolePresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        super.setVisible(true);
    }

}
