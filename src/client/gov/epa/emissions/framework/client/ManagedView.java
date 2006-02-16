package gov.epa.emissions.framework.client;


public interface ManagedView extends EmfView {

    void bringToFront();

    String getName();

    boolean isAlive();
    
    boolean hasChanges();
    
    void resetChanges();
    
    boolean checkChanges(); //FIXME: do we need this method
}
