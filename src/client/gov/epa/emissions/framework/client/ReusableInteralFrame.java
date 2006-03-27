package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.console.DesktopManager;

import java.awt.Dimension;


public abstract class ReusableInteralFrame extends EmfInternalFrame {

    public ReusableInteralFrame(String title, Dimension dimension, DesktopManager desktopManager) {
        super(title, dimension, desktopManager);
    }

    public ReusableInteralFrame(String title, DesktopManager desktopManager) {
        super(title, desktopManager);
    }

    public void windowClosing() {
        close();
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
        return true;// never terminate, until the application does
    }

}
