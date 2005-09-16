package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.status.StatusWindow;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

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

        menubar.add(createFileMenu(session, desktop));
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

    private JMenu createFileMenu(EmfSession session, JDesktopPane desktop) {
        return new FileMenu(session, this, desktop, messagePanel);
    }

    public void close() {
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
