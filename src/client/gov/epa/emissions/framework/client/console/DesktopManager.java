package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

public interface DesktopManager {
    
    public void registerOpenWindow(ManagedView manageView);
    
    public void unregisterCloseWindow(ManagedView manageView);

    public void closeAll();
    
}