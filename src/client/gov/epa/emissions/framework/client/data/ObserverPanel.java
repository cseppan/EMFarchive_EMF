package gov.epa.emissions.framework.client.data;

public interface ObserverPanel {
    void update(int changes);
    
    int getPreviousNumber();
}
