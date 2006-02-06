package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DesktopManagerImpl implements DesktopManager {

    private WindowMenuView windowMenu;

    private Map windowNames;

    public DesktopManagerImpl(WindowMenuView windowMenu) {
        this.windowMenu = windowMenu;

        this.windowNames = new HashMap();
    }

    public void registerOpenWindow(ManagedView manageView) {
        String name = manageView.getName();
        if (!windowNames.containsKey(name)) {
            windowNames.put(name, manageView);
            windowMenu.register(manageView);
        }
        // setposition
    }

    public void unregisterCloseWindow(ManagedView manageView) {
        windowNames.remove(manageView.getName());
        windowMenu.unregister(manageView);
    }

    public void unregisterFromWindowMenu(ManagedView view) {
        view.close();
    }

    public void closeAll() {
        Iterator iterator = windowNames.keySet().iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            ManagedView view = (ManagedView) windowNames.get(key);
            windowMenu.unregister(view);
            windowNames.remove(key);
        }
    }

}