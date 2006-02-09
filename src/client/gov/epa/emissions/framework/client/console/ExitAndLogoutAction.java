package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.commons.gui.ConfirmDialog;

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
            closeAll();
        }
        return false;
    }

    public boolean exit() {
        String message = "Do you want to exit?";
        if (confirm(message)) {
            closeAll();
            System.exit(0);
        }
        return false;
    }

    private boolean confirm(String message) {
        ConfirmDialog dialog = new ConfirmDialog(message, "Warning", emfConsole);
        return dialog.confirm();
    }

    private void closeAll() {
        desktopManager.closeAll();
        emfConsole.close();
    }

}
