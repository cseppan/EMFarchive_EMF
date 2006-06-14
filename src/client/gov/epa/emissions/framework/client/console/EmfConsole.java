package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.commons.gui.Confirm;
import gov.epa.emissions.framework.ConcurrentTaskRunner;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.status.StatusPresenter;
import gov.epa.emissions.framework.client.status.StatusWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.Dimensions;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.YesNoDialog;

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

    private MessagePanel messagePanel;

    private WindowMenuPresenter windowMenuPresenter;

    private ManageMenu manageMenu;

    private StatusPresenter presenter;

    private static String aboutMessage = "<html><center>Emissions Modeling Framework (EMF)<br>"
            + "Version 1.3.3 - 6/14/2006<br>" + "Developed by the Carolina Environmental Program<br>"
            + "University of North Carolina at Chapel Hill</center></html>";

    private DesktopManager desktopManager;

    private Confirm emfConfirmDialog;

    private EmfSession session;

    public EmfConsole(EmfSession session) {
        super("EMF Console", "Emissions Modeling Framework (EMF):  " + session.user().getName() + "("
                + session.user().getUsername() + ")");
        this.session = session;

        setProperties();
        createLayout(session);
        showStatus();
    }

    private void createLayout(EmfSession session) {
        WindowMenu windowMenu = createWindowMenu();

        this.desktopManager = createDesktopManager(windowMenu);
        this.windowMenuPresenter.setDesktopManager(desktopManager);

        super.setJMenuBar(createMenuBar(windowMenu, session));
    }

    private DesktopManager createDesktopManager(WindowMenu windowMenu) {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        this.setContentPane(desktop);

        return new DesktopManagerImpl(windowMenu, this, new EmfDesktopImpl(desktop));
    }

    private void showStatus() {
        StatusWindow status = new StatusWindow(this, desktopManager);
        windowMenuPresenter.addPermanently(status);

        presenter = new StatusPresenter(session.user(), session.dataCommonsService(), new ConcurrentTaskRunner());
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

    private JMenuBar createMenuBar(WindowMenu windowMenu, EmfSession session) {
        JMenuBar menubar = new JMenuBar();

        messagePanel = new SingleLineMessagePanel();

        menubar.add(createFileMenu(session, messagePanel));
        menubar.add(createManageMenu(session, messagePanel));
        menubar.add(windowMenu);
        menubar.add(createHelpMenu());

        menubar.add(messagePanel);

        return menubar;
    }

    private WindowMenu createWindowMenu() {
        WindowMenu windowMenu = new WindowMenu();
        windowMenuPresenter = new WindowMenuPresenter(windowMenu);
        windowMenu.setWindowMenuViewPresenter(windowMenuPresenter);

        return windowMenu;
    }

    private JMenu createFileMenu(EmfSession session, MessagePanel messagePanel) {
        return new FileMenu(session, this, messagePanel, desktopManager);
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

    private JMenu createManageMenu(EmfSession session, MessagePanel messagePanel) {
        manageMenu = new ManageMenu(session, this, messagePanel, desktopManager);
        new ManageMenuPresenter(manageMenu, session).observe();
        
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

    public void displayUserManager() throws EmfException {
        manageMenu.displayUserManager();
    }

    public boolean confirm() {
        emfConfirmDialog = emfConfirmDialog();
        return emfConfirmDialog.confirm();
    }

    private Confirm emfConfirmDialog() {
        String msg = "Some windows have unsaved changes.\nDo you want to continue closing these windows?";
        Confirm emfConfirmDialog = new YesNoDialog(this, "Unsaved Changes Exist", msg);
        return emfConfirmDialog;
    }

}
