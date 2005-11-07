package gov.epa.emissions.framework.client;

public interface ChangesNotifier {
    void observeChanges(ChangeObserver listener);
}
