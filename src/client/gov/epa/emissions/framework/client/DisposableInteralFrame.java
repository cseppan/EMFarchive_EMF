package gov.epa.emissions.framework.client;

public abstract class DisposableInteralFrame extends EmfInternalFrame {

    public DisposableInteralFrame(String title) {
        super(title);
    }

    public void close() {
        super.dispose();
    }

    public boolean isAlive() {
        return !super.isClosed();
    }
}
