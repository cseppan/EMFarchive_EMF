package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.ui.ViewLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class FileMenu extends JMenu {

    private ViewLayout viewLayout;

    private DesktopManager desktopManager;

    // FIXME: where's the associated Presenter ?
    public FileMenu(EmfSession session, EmfConsole parent, MessagePanel messagePanel, ViewLayout viewLayout,
            DesktopManager desktopManager) {
        super("File");
        super.setName("file");

        super.add(createImport(session, parent.desktop(), messagePanel));
        super.addSeparator();
        super.add(createLogout(session, parent));
        super.add(createExit(parent));

        this.viewLayout = viewLayout;
        this.desktopManager = desktopManager;
    }

    private JMenuItem createExit(final EmfConsole parent) {
        JMenuItem exit = new JMenuItem("Exit");
        exit.setName("exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ExitAndLogoutAction exit = new ExitAndLogoutAction(parent, desktopManager);
                exit.exit();
            }
        });
        return exit;
    }

    private JMenuItem createLogout(final EmfSession session, final EmfConsole parent) {
        JMenuItem logout = new JMenuItem("Logout");
        logout.setName("logout");
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout(session, parent, desktopManager);
            }
        });
        return logout;
    }

    private void logout(EmfSession session, EmfConsole parent, DesktopManager desktopManager) {
        ExitAndLogoutAction logout = new ExitAndLogoutAction(parent, desktopManager);
        if (logout.logout()) {
            UserService userServices = session.userService();
            LoginWindow view = new LoginWindow(session.serviceLocator());

            LoginPresenter presenter = new LoginPresenter(userServices);
            presenter.display(view);
        }

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
                } catch (Exception e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        return importMenu;
    }

    protected void displayImport(EmfSession session, JDesktopPane desktop) throws EmfException, Exception {
        if (viewLayout.activate("Import Dataset - FileMenu"))
            return;

        ImportWindow importView = new ImportWindow(session.dataCommonsService(), desktop, desktopManager);
        viewLayout.add(importView, "Import Dataset - FileMenu");
        desktop.add(importView);

        ImportPresenter presenter = new ImportPresenter(session, session.user(), session.eximService());
        presenter.display(importView);
    }

}
