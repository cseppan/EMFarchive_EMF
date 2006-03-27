package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.console.DesktopManager;

//FIXME: refactor the common behavior b/w this and ReusableInternalFrame
public class UpdateMyProfileWindow extends UpdateUserWindow {

    public UpdateMyProfileWindow(DesktopManager desktopManager) {
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

}
