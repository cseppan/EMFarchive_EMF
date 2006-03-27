package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.console.DesktopManager;

public class DisposableUpdateUserWindow extends UpdateUserWindow {

    public DisposableUpdateUserWindow(AdminOption adminOption, DesktopManager desktopManager) {
        super(adminOption, desktopManager);
    }

    public DisposableUpdateUserWindow(DesktopManager desktopManager) {
        super(desktopManager);
    }

    public void windowClosing() {
        super.dispose();
    }

    public boolean isAlive() {
        return !super.isClosed();
    }

}
