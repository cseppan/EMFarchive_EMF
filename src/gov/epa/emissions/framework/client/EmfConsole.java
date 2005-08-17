package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.admin.UpdateUserPresenter;
import gov.epa.emissions.framework.client.admin.UpdateUserWindow;
import gov.epa.emissions.framework.client.admin.UserManagerPresenter;
import gov.epa.emissions.framework.client.admin.UserManagerWindow;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.commons.ExImServices;
import gov.epa.emissions.framework.commons.User;
import gov.epa.emissions.framework.commons.UserServices;

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

    private UserServices userAdmin;

    private JDesktopPane desktop;

    private User user;

    private EmfConsolePresenter presenter;

    // TODO: should we user 'ServiceLocator' instead, since other services will
    // also be needed

    // TODO: split the login & logout menu/actions in a separate class ??
    public EmfConsole(User user, UserServices userAdmin) {
        this.user = user;
        this.userAdmin = userAdmin;

        super.setJMenuBar(createMenuBar());

        super.setSize(new Dimension(900, 700));
        super.setLocation(new Point(300, 150));
        super.setTitle("Emissions Modeling Framework (EMF)");
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        this.setContentPane(desktop);
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
        ExImServices eximServices = null;//TODO: fetch it using ServiceLocator
        ImportWindow view = new ImportWindow(user, eximServices);
        ImportPresenter presenter = new ImportPresenter(user, eximServices, view);
        presenter.observe();
        
        desktop.add(view);

        view.display();
    }

    private void logout() {
        LoginWindow view = new LoginWindow(userAdmin);
        LoginPresenter presenter = new LoginPresenter(userAdmin, view);
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
        UpdateUserPresenter presenter = new UpdateUserPresenter(userAdmin, view);
        presenter.observe();

        desktop.add(view);

        view.display();
    }

    public void displayUserManager() {
        try {
            UserManagerWindow console = new UserManagerWindow(userAdmin, this);
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
