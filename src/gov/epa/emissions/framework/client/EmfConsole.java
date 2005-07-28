package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.admin.UserManagerConsole;
import gov.epa.emissions.framework.client.admin.UserManagerPresenter;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class EmfConsole extends EmfWindow {

    private EMFUserAdmin userAdmin;

    private JDesktopPane desktop;

    // TODO: should we user 'ServiceLocator' instead, since other services will
    // also be needed
    public EmfConsole(EMFUserAdmin userAdmin) {
        this.userAdmin = userAdmin;

        this.setJMenuBar(createMenuBar());

        this.setSize(new Dimension(900, 700));
        this.setLocation(new Point(300, 150));
        this.setTitle("Emissions Modeling Framework (EMF)");

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

        menu.add(createDisabledMenuItem("Import"));
        menu.add(createDisabledMenuItem("Export"));
        menu.addSeparator();
        menu.add(createDisabledMenuItem("Logout"));

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

        JMenuItem users = new JMenuItem("Users");
        users.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                launchUsersManagementWindow();
            }
        });
        menu.add(users);

        return menu;
    }

    private void launchUsersManagementWindow() {
        try {
            UserManagerConsole console = new UserManagerConsole(userAdmin);
            UserManagerPresenter presenter = new UserManagerPresenter(userAdmin, console);
            presenter.observe();
            
            desktop.add(console);
            console.setVisible(true);
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

}
