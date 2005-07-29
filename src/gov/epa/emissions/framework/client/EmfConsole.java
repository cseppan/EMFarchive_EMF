package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UpdateUserWindow;
import gov.epa.emissions.framework.client.admin.UserManagerPresenter;
import gov.epa.emissions.framework.client.admin.UserManagerWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.SimpleLoginWindow;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

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

    private EMFUserAdmin userAdmin;

    private JDesktopPane desktop;

    private User user;

    private EmfConsolePresenter presenter;

    // TODO: should we user 'ServiceLocator' instead, since other services will
    // also be needed

    // TODO: split the login & logout menu/actions in a separate class ??
    public EmfConsole(User user, EMFUserAdmin userAdmin) {
        this.user = user;
        this.userAdmin = userAdmin;

        super.setJMenuBar(createPostLoginMenuBar());

        super.setSize(new Dimension(900, 700));
        super.setLocation(new Point(300, 150));
        super.setTitle("Emissions Modeling Framework (EMF)");
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        this.setContentPane(desktop);
    }

    private JMenuBar createPostLoginMenuBar() {
        JMenuBar menubar = new JMenuBar();

        menubar.add(createPostLoginFileMenu());
        menubar.add(createManageMenu());
        menubar.add(createHelpMenu());

        return menubar;
    }

    private JMenu createPostLoginFileMenu() {
        JMenu menu = new JMenu("File");

        menu.add(createDisabledMenuItem("Import"));
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

    private void logout() {
        user = null;
        super.setJMenuBar(createPreLoginMenuBar());

        refresh();
    }

    private JMenuBar createPreLoginMenuBar() {
        JMenuBar menubar = new JMenuBar();
        menubar.add(createPreLoginFileMenu());
        menubar.add(createHelpMenu());

        return menubar;
    }

    private JMenu createPreLoginFileMenu() {
        JMenu menu = new JMenu("File");

        JMenuItem login = new JMenuItem("Login");
        login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                login();
            }
        });
        menu.add(login);

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

    private void login() {
        SimpleLoginWindow view = new SimpleLoginWindow(userAdmin);

        LoginPresenter presenter = new LoginPresenter(userAdmin, view);
        presenter.observe();

        desktop.add(view);
        view.display();
    }

    private void refresh() {
        super.validate();
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
        UpdateUserPresenter presenter = new UpdateUserPresenter(userAdmin, view);
        presenter.observe();

        desktop.add(view);

        view.display();
    }

    public void displayUserManager() {
        try {
            UserManagerWindow console = new UserManagerWindow(userAdmin);
            UserManagerPresenter presenter = new UserManagerPresenter(userAdmin, console);
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
