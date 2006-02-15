package gov.epa.emissions.framework.client;

public interface ManagedView extends EmfView {

    void bringToFront();

    String getName();

    boolean isAlive();
}
