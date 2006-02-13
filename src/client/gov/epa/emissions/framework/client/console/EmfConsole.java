package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.ConcurrentTaskRunner;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.status.StatusPresenter;
import gov.epa.emissions.framework.client.status.StatusWindow;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.CascadeLayout;
import gov.epa.emissions.framework.ui.Dimensions;
import gov.epa.emissions.framework.ui.ViewLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
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

    private WindowMenuPresenter windowMenuPresenter;

    private ManageMenu manageMenu;

    private StatusPresenter presenter;

    private ViewLayout viewLayout;

    private static String aboutMessage = "<html><center>Emissions Modeling Framework (EMF)<br>"
            + "Version: Beta 2.1 - 1/30/2006<br>" + "Developed by the Carolina Environmental Program<br>"
            + "University of North Carolina at Chapel Hill</center></html>";

    private WindowMenuView windowMenuView;

    private DesktopManager desktopManager;

    // TODO: split the login & logout menu/actions in a separate class ??
    public EmfConsole(EmfSession session) {
        super("EMF Console", "Emissions Modeling Framework (EMF):  " + session.user().getName() + "("
                + session.user().getUsername() + ")");
        user = session.user();
        this.serviceLocator = session.serviceLocator();
        windowMenuView = createWindowMenu();
        this.desktopManager = new DesktopManagerImpl(windowMenuView, this);
        this.windowMenuPresenter.setDesktopManager(desktopManager);
        this.windowMenuView.setWindowMenuViewPresenter(windowMenuPresenter);
        this.viewLayout = new CascadeLayout(this);
        messagePanel = new SingleLineMessagePanel();

        setProperties();
        setLayout(session);
        showStatus();
    }

    private void setLayout(EmfSession session) {
        desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        this.setContentPane(desktop);

        JMenuBar menuBar = createMenuBar(session);
        super.setJMenuBar(menuBar);

        menuBar.add(messagePanel);
    }

    private void showStatus() {
        DataCommonsService statusServices = serviceLocator.dataCommonsService();
        StatusWindow status = new StatusWindow(this, desktop, desktopManager);
        windowMenuPresenter.notifyAdd(status);

        desktop.add(status);

        presenter = new StatusPresenter(user, statusServices, new ConcurrentTaskRunner());
        presenter.display(status);
    }

    private void setProperties() {
        setSize();
        super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        super.setResizable(true);
    }

    protected void windowClosing() {// overriden
        ExitAndLogoutAction exitAction = new ExitAndLogoutAction(EmfConsole.this, desktopManager);
        exitAction.exit();
    }

    private void setSize() {
        Dimension dim = new Dimensions().getSize(0.9, 0.9);
        super.setSize(dim);
    }

    private JMenuBar createMenuBar(EmfSession session) {
        JMenuBar menubar = new JMenuBar();

        menubar.add(createFileMenu(session));
        menubar.add(createManageMenu(session));
        menubar.add((WindowMenu) windowMenuView);
        menubar.add(createHelpMenu());

        return menubar;
    }

    private WindowMenuView createWindowMenu() {
        WindowMenuView windowsMenu = new WindowMenu();
        windowMenuPresenter = new WindowMenuPresenter(windowsMenu);
        return windowsMenu;
    }

    private JMenu createFileMenu(EmfSession session) {
        return new FileMenu(session, this, messagePanel, viewLayout, desktopManager);
    }

    public void doClose() {
        // TODO: auto logout of a session
        presenter.close();
    }

    private JMenuItem createDisabledMenuItem(String name) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.setEnabled(false);

        return menuItem;
    }

    private JMenu createManageMenu(EmfSession session) {
        manageMenu = new ManageMenu(session, this, messagePanel, viewLayout, desktopManager);

        return manageMenu;
    }

    private JMenu createHelpMenu() {
        JMenu menu = new JMenu("Help");

        menu.add(createDisabledMenuItem("User Guide"));
        menu.add(createDisabledMenuItem("Documentation"));

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showMessageDialog(EmfConsole.this, aboutMessage, "About the EMF",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(about);

        return menu;
    }

    public void observe(EmfConsolePresenter presenter) {
        manageMenu.observe(presenter);
    }

    public void displayUserManager() {
        manageMenu.displayUserManager();
    }

    public JDesktopPane desktop() {
        return desktop;
    }

    public void addToDesktop(Component view) {
        desktop.add(view);
    }

}
