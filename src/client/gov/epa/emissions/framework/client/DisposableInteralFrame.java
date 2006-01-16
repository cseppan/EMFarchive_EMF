package gov.epa.emissions.framework.client;

import java.awt.Dimension;

public abstract class DisposableInteralFrame extends EmfInternalFrame {

    public DisposableInteralFrame(String title) {
        super(title);
    }

    public DisposableInteralFrame(String title, Dimension dimension) {
        super(title, dimension);
    }

    public void windowClosing() {
        close();
    }

    final public void close() {
        super.dispose();
    }

    public boolean isAlive() {
        return !super.isClosed();
    }
}
