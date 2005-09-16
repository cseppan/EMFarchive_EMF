package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
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

    private ServiceLocator serviceLocator;

    private MessagePanel messagePanel;

    private StatusWindow status;

    private WindowMenuPresenter windowMenuPresenter;

    private ImportWindow importView;

    private ManageMenu manageMenu;

    // TODO: split the login & logout menu/actions in a separate class ??
    public EmfConsole(EmfSession session) {
        super("EMF Console", "Emissions Modeling Framework (EMF)");
        user = session.getUser();
        this.serviceLocator = session.getServiceLocator();

        setProperties();
        setLayout(session);
        showStatus();
    }

    private void setLayout(EmfSession session) {
        desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        this.setContentPane(desktop);

        JMenuBar menuBar = createMenuBar(session, desktop);
        super.setJMenuBar(menuBar);

        messagePanel = new SingleLineMessagePanel();
        menuBar.add(messagePanel);
    }

    private void showStatus() {
        StatusServices statusServices = serviceLocator.getStatusServices();
        status = new StatusWindow(user, statusServices, this, desktop);
        windowMenuPresenter.notifyAdd(status);

        desktop.add(status);

        status.display();
    }

    private void setProperties() {
        super.setSize(new Dimension(900, 700));
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuBar createMenuBar(EmfSession session, JDesktopPane desktop) {
        JMenuBar menubar = new JMenuBar();

        menubar.add(createFileMenu());
        menubar.add(createManageMenu(session, desktop));
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
        if (importView != null) {
            importView.bringToFront();
            return;
        }

        ExImServices eximServices = serviceLocator.getExImServices();

        importView = new ImportWindow(eximServices, desktop);
        desktop.add(importView);

        ImportPresenter presenter = new ImportPresenter(user, eximServices);
        presenter.observe(importView);
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

    private JMenu createManageMenu(EmfSession session, JDesktopPane desktop) {
        manageMenu = new ManageMenu(session, this, desktop, messagePanel);

        return manageMenu;
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

    public void observe(EmfConsolePresenter presenter) {
        manageMenu.observe(presenter);
    }

    public void display() {
        super.setVisible(true);
    }

    public void displayUserManager() {
        manageMenu.displayUserManager();
    }

}
