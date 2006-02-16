package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;

public class DisposableUpdateUserWindow extends UpdateUserWindow {

    public DisposableUpdateUserWindow(EmfConsole parentConsole, AdminOption adminOption, DesktopManager desktopManager) {
        super(parentConsole, adminOption, desktopManager);
    }

    public DisposableUpdateUserWindow(EmfConsole parentConsole, DesktopManager desktopManager) {
        super(parentConsole, desktopManager);
    }

    public void close() {
        super.dispose();
        super.close();
    }

    public boolean isAlive() {
        return !super.isClosed();
    }

}
