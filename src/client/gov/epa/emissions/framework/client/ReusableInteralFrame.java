package gov.epa.emissions.framework.client;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDesktopPane;

public abstract class ReusableInteralFrame extends EmfInternalFrame {

    protected JDesktopPane desktop;

    public ReusableInteralFrame(String title, JDesktopPane desktop) {
        super(title);
        this.desktop = desktop;
    }

    public ReusableInteralFrame(String title, Dimension dimension, JDesktopPane desktop) {
        super(title, dimension);
        this.desktop = desktop;
    }

    public void close() {
        super.setVisible(false);
    }

    public void bringToFront() {
        ensurePresenceOnDesktop(desktop);
        super.bringToFront();
    }

    private void ensurePresenceOnDesktop(JDesktopPane desktop) {
        List componentsList = Arrays.asList(desktop.getAllFrames());
        if (!componentsList.contains(this)) {
            desktop.add(this);
        }
    }

    public boolean isAlive() {
        return true;// never terminate, until the application does
    }

}
