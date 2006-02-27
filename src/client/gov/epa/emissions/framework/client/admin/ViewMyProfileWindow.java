package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.console.DesktopManager;

public class ViewMyProfileWindow extends ViewUserWindow {

    public ViewMyProfileWindow(DesktopManager desktopManager) {
        super(desktopManager);
    }

    public void close() {
        super.setVisible(false);
        super.close();
    }

    public void bringToFront() {
        desktopManager.ensurePresence(this);
        super.bringToFront();
    }

    public boolean isAlive() {
        return true;// never terminates, similar to ReusableInternalFrame
    }

    public void windowClosing() {
        close();
    }
}
