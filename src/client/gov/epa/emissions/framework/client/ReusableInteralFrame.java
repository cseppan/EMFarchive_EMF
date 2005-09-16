package gov.epa.emissions.framework.client;

public abstract class ReusableInteralFrame extends EmfInternalFrame {

    public ReusableInteralFrame(String title) {
        super(title);
    }

    public void close() {
        super.setVisible(false);
    }
}
