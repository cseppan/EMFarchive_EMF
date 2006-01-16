package gov.epa.emissions.framework.client.admin;

public class DisposableViewUserWindow extends ViewUserWindow {

    public void close() {
        super.dispose();
    }

    public boolean isAlive() {
        return !super.isClosed();
    }

    public void windowClosing() {
        close();
    }
}
