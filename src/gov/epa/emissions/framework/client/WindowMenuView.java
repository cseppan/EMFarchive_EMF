package gov.epa.emissions.framework.client;

public interface WindowMenuView {
    void register(ManagedView view);
    void unregister(ManagedView view);
}
