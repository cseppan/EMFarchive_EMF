package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.console.DesktopManager;

public class DisposableViewUserWindow extends ViewUserWindow {

    public DisposableViewUserWindow(DesktopManager desktopManager) {
        super(desktopManager);
    }

    public void disposeView() {
        super.dispose();
        super.disposeView();
    }

    public boolean isAlive() {
        return !super.isClosed();
    }

    public void windowClosing() {
        disposeView();
    }
}
