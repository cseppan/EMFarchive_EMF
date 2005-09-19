package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class FileMenu extends JMenu {

    private ImportWindow importView;

    public FileMenu(EmfSession session, EmfConsole parent, JDesktopPane desktop, MessagePanel messagePanel) {
        super("File");
        super.setName("file");

        super.add(createImport(session, desktop, messagePanel));
        super.addSeparator();
        super.add(createLogout(session, parent));
        super.add(createExit());
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
        UserServices userServices = session.getUserServices();
        LoginWindow view = new LoginWindow(session.getServiceLocator());
        
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
        if (importView != null) {
            importView.bringToFront();
            return;
        }

        ServiceLocator serviceLocator = session.getServiceLocator();
        ExImServices eximServices = serviceLocator.getExImServices();

        importView = new ImportWindow(eximServices, desktop);
        desktop.add(importView);

        ImportPresenter presenter = new ImportPresenter(session.getUser(), eximServices);
        presenter.display(importView);
    }

}
