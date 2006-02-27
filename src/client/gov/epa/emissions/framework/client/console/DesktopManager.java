package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

public interface DesktopManager {

    public void openWindow(ManagedView manageView);

    public void closeWindow(ManagedView manageView);

    public boolean closeAll();

    public void ensurePresence(ManagedView frame);

}