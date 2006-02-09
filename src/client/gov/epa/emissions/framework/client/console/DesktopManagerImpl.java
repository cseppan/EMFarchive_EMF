package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    }

    public void unregisterCloseWindow(ManagedView manageView) {
        windowNames.remove(manageView.getName());
        windowMenu.unregister(manageView);
    }

    public void closeAll() {
        List list = new ArrayList(windowNames.keySet());
        for (int i = 0; i < list.size(); i++) {
            Object key = list.get(i);
            ManagedView view = (ManagedView) windowNames.get(key);
            view.close();//unregisterCloseWindow is called inside this method
        }
    }

}