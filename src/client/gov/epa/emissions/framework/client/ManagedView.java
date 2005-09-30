package gov.epa.emissions.framework.client;

public interface ManagedView extends EmfView {
    String getTitle();

    void bringToFront();

    String getName();

    boolean isAlive();
}
