package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UpdateUserWindow;
import gov.epa.emissions.framework.client.admin.UserManagerPresenter;
import gov.epa.emissions.framework.client.admin.UserManagerWindow;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class EmfConsole extends EmfWindow implements EmfConsoleView {

    private JDesktopPane desktop;

    private User user;

    private EmfConsolePresenter presenter;

    private ServiceLocator serviceLocator;

    // TODO: split the login & logout menu/actions in a separate class ??
    public EmfConsole(User user, ServiceLocator serviceLocator) {
        this.user = user;
        this.serviceLocator = serviceLocator;

        super.setJMenuBar(createMenuBar());
        setProperties();

        desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        this.setContentPane(desktop);
        showStatus();
    }

    private void showStatus() {
        StatusServices statusServices = serviceLocator.getStatusServices();
        StatusWindow view = new StatusWindow(user, statusServices);

        desktop.add(view);

        view.display();
    }

    private void setProperties() {
        super.setSize(new Dimension(900, 700));
        super.setLocation(new Point(300, 150));
        super.setTitle("Emissions Modeling Framework (EMF)");
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menubar = new JMenuBar();

        menubar.add(createFileMenu());
        menubar.add(createManageMenu());
        menubar.add(createHelpMenu());

        return menubar;
    }

    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");

        JMenuItem importMenu = new JMenuItem("Import");
        importMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                displayImport();
            }
        });
        menu.add(importMenu);

        menu.add(createDisabledMenuItem("Export"));
        menu.addSeparator();

        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout();
            }
        });
        menu.add(logout);

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // TODO: logout before exiting. Should prompt the user ?
                System.exit(0);
            }
        });
        menu.add(exit);

        return menu;
    }

    protected void displayImport() {
        ExImServices eximServices = serviceLocator.getEximServices();
        ImportWindow view = new ImportWindow(user, eximServices);
        ImportPresenter presenter = new ImportPresenter(user, eximServices, view);
        presenter.observe();

        desktop.add(view);

        view.display();
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
        super.dispose();
    }

    private JMenuItem createDisabledMenuItem(String name) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setEnabled(false);

        return menuItem;
    }

    private JMenu createManageMenu() {
        JMenu menu = new JMenu("Manage");

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
            UserManagerWindow console = new UserManagerWindow(userServices, this);
            UserManagerPresenter presenter = new UserManagerPresenter(userServices, console);
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
