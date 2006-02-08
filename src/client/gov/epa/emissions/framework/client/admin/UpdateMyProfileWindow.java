package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.console.DesktopManager;

import java.util.Arrays;
import java.util.List;

import javax.swing.JDesktopPane;

//FIXME: refactor the common behavior b/w this and ReusableInternalFrame
public class UpdateMyProfileWindow extends UpdateUserWindow {

    private JDesktopPane desktop;

    public UpdateMyProfileWindow(User user, JDesktopPane desktop, DesktopManager desktopManager) {
        super(user, desktopManager);
        this.desktop = desktop;
    }

    public void close() {
        super.setVisible(false);
    }

    public void bringToFront() {
        ensurePresenceOnDesktop(desktop);
        super.bringToFront();
    }

    public boolean isAlive() {
        return true;// never terminates, similar to ReusableInternalFrame
    }

    private void ensurePresenceOnDesktop(JDesktopPane desktop) {
        List componentsList = Arrays.asList(desktop.getAllFrames());
        if (!componentsList.contains(this)) {
            desktop.add(this);
        }
    }

    public void windowClosing() {
        close();
    }
}
