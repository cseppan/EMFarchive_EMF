package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.ui.YesNoDialog;

public class ExitAndLogoutAction {

    private EmfConsole emfConsole;

    private DesktopManager desktopManager;

    public ExitAndLogoutAction(EmfConsole parent, DesktopManager desktopManager) {
        this.emfConsole = parent;
        this.desktopManager = desktopManager;
    }

    public boolean logout() {
        String message = "Do you want to log out of the Emission Modeling Framework?";
        if (confirm(message)) {
            if (desktopManager.closeAll()) {
                emfConsole.disposeView();
                emfConsole.logExitMessage();
                return true;
            }
        }
        return false;
    }

    public boolean exit() {
        String message = "Do you want to exit the Emission Modeling Framework?";
        if (confirm(message)) {
            if (desktopManager.closeAll()) {
                emfConsole.disposeView();
                emfConsole.logExitMessage();
                System.exit(0);
            }
        }
        return false;
    }

    private boolean confirm(String message) {
        YesNoDialog emfDialog = new YesNoDialog(emfConsole, "Logout/Exit Confirmation", message);
        return emfDialog.confirm();
    }

}
