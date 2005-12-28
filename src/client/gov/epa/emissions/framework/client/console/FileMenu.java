package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.ViewLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class FileMenu extends JMenu {

    private ViewLayout viewLayout;

    // FIXME: where's the associated Presenter ?
    public FileMenu(EmfSession session, EmfConsole parent, MessagePanel messagePanel, ViewLayout windowLayoutManager) {
        super("File");
        super.setName("file");

        super.add(createImport(session, parent.desktop(), messagePanel));
        super.addSeparator();
        super.add(createLogout(session, parent));
        super.add(createExit());

        this.viewLayout = windowLayoutManager;
    }

    private JMenuItem createExit() {
        JMenuItem exit = new JMenuItem("Exit");
        exit.setName("exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // TODO: logout before exiting. Should prompt the user ?
                System.exit(0);
            }
        });
        return exit;
    }

    private JMenuItem createLogout(final EmfSession session, final EmfConsole parent) {
        JMenuItem logout = new JMenuItem("Logout");
        logout.setName("logout");
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout(session, parent);
            }
        });
        return logout;
    }

    private void logout(EmfSession session, EmfConsole parent) {
        UserService userServices = session.userService();
        LoginWindow view = new LoginWindow(session.serviceLocator());

        LoginPresenter presenter = new LoginPresenter(userServices);
        presenter.display(view);

        parent.close();
    }

    private JMenuItem createImport(final EmfSession session, final JDesktopPane desktop, final MessagePanel messagePanel) {
        JMenuItem importMenu = new JMenuItem("Import");
        importMenu.setName("import");
        importMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displayImport(session, desktop);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        return importMenu;
    }

    protected void displayImport(EmfSession session, JDesktopPane desktop) throws EmfException {
        if (viewLayout.activate("Import Dataset - FileMenu"))
            return;

        ServiceLocator serviceLocator = session.serviceLocator();
        ExImService eximServices = serviceLocator.eximService();

        ImportWindow importView = new ImportWindow(serviceLocator.datasetTypeService(), desktop);
        viewLayout.add(importView, "Import Dataset - FileMenu");
        desktop.add(importView);

        ImportPresenter presenter = new ImportPresenter(session.user(), eximServices);
        presenter.display(importView);
    }

}
