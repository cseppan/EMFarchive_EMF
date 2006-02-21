package gov.epa.emissions.framework.client;

public interface ChangeObserver {
    void onChange();
    
    void alert(boolean alert);
}
