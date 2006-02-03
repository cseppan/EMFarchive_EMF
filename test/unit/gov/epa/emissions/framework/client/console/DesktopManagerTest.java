package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DesktopManagerTest extends MockObjectTestCase {

    public void testShouldRegisterOpenWindowWithWindowMenuOnRegister() {
        Mock windowsMenu = mock(WindowMenuView.class);
        
        ManagedView managedViewProxy = (ManagedView) mock(ManagedView.class).proxy();
        windowsMenu.expects(once()).method("register").with(same(managedViewProxy));
        
        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()));
        desktopManager.registerOpenWindow(managedViewProxy);
    }

}