package gov.epa.emissions.framework.client;

public abstract class DisposableEmfInteralFrame extends EmfInternalFrame {

    public DisposableEmfInteralFrame(String title) {
        super(title);
    }

    public void close() {
        super.dispose();
    }
}
