package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.console.DesktopManager;

import java.util.Arrays;
import java.util.List;

import javax.swing.JDesktopPane;

public class ViewMyProfileWindow extends ViewUserWindow {

    private JDesktopPane desktop;

    public ViewMyProfileWindow(JDesktopPane desktop, DesktopManager desktopManager) {
        super(desktopManager);
        this.desktop = desktop;
    }

    public void close() {
        super.setVisible(false);
        super.close();
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
