package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.console.DesktopManager;

public class DisposableViewUserWindow extends ViewUserWindow {

    public DisposableViewUserWindow(DesktopManager desktopManager) {
        super(desktopManager);
    }

    public void windowClosing() {
        super.dispose();
    }

    public boolean isAlive() {
        return !super.isClosed();
    }

}
