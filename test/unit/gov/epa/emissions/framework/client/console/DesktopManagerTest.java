package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class DesktopManagerTest extends MockObjectTestCase {

    public void testShouldRegisterOpenWindowWithWindowMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);
        
        Mock managedView = mock(ManagedView.class);
        ManagedView managedViewProxy = (ManagedView) managedView.proxy();
        managedView.expects(once()).method("getName").withNoArguments();
        windowsMenu.expects(once()).method("register").with(same(managedViewProxy));
        
        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()));
        desktopManager.registerOpenWindow(managedViewProxy);
    }
    
    public void testShouldUnRegisterCloseWindowWithWindowMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);
        Mock managedView = mock(ManagedView.class);
        
        ManagedView managedViewProxy = (ManagedView) managedView.proxy();
        managedView.expects(once()).method("getName").withNoArguments();
        windowsMenu.expects(once()).method("unregister").with(same(managedViewProxy));
        
        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()));
        desktopManager.unregisterCloseWindow(managedViewProxy);
    }
    
    public void itestShouldCloseAllWindowsAndUnRegisterFromWindowMenu(){
        Mock managedView1 = manageView();
        Mock managedView2 = manageView();
        
        Mock windowsMenu = windoswMenu();
        
        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()));
        desktopManager.registerOpenWindow((ManagedView) managedView1.proxy());
        desktopManager.registerOpenWindow((ManagedView) managedView2.proxy());
        
        desktopManager.closeAll();
    }

    private Mock windoswMenu() {
        Mock managedView = mock(ManagedView.class);
        
        Mock windowsMenu = mock(WindowMenuView.class);
        windowsMenu.stubs().method("register").with(same(managedView.proxy()));
        windowsMenu.stubs().method("unregister").with(same(managedView.proxy()));
        return windowsMenu;
    }


    private Mock manageView() {
        Mock managedView = mock(ManagedView.class);
        managedView.stubs().method("close").withNoArguments();
        managedView.stubs().method("getName").withNoArguments();
        return managedView;
    }
    

}