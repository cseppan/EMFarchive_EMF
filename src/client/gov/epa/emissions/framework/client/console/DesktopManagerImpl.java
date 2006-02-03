package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

public class DesktopManagerImpl implements DesktopManager {

    private WindowMenuView windowMenu;

    public DesktopManagerImpl(WindowMenuView windowMenu) {
        this.windowMenu = windowMenu;
        //windowMenu.setDesktopManager(this);
    }

    public void registerOpenWindow(ManagedView manageView) {
        //TODO If it's already exist in the map what to do?
        windowMenu.register(manageView);
    }
}