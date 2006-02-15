package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.ui.EmfDialog;

import javax.swing.JOptionPane;

public class ExitAndLogoutAction {

    private EmfConsole emfConsole;

    private DesktopManager desktopManager;

    public ExitAndLogoutAction(EmfConsole parent, DesktopManager desktopManager) {
        this.emfConsole = parent;
        this.desktopManager = desktopManager;
    }

    public boolean logout() {
        String message = "Do you want to logout?";
        if (confirm(message)) {
            if (desktopManager.closeAll()) {
                emfConsole.close();
                return true;
            }
        }
        return false;
    }

    public boolean exit() {
        String message = "Do you want to exit?";
        if (confirm(message)) {
            if (desktopManager.closeAll()) {
                emfConsole.close();
                System.exit(0);
            }
        }
        return false;
    }

    private boolean confirm(String message) {
        EmfDialog emfDialog = new EmfDialog(emfConsole, "Warning", JOptionPane.QUESTION_MESSAGE, message,
                JOptionPane.OK_CANCEL_OPTION);
        return emfDialog.confirm();
    }

}
