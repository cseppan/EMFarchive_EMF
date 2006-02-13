package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.ManagedView;

public interface Layout {
    
    void position(ManagedView managedView);

    void unregister(ManagedView managedView);

}
