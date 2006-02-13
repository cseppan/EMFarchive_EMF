package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class DesktopManagerTest extends MockObjectTestCase {

    public void testShouldRegisterOpenWindowWithWindowMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);

        Mock managedView = mock(ManagedView.class);
        managedView.expects(once()).method("bringToFront").withNoArguments();

        Mock emfConsole = mock(EmfConsoleView.class);
        emfConsole.expects(once()).method("width").withNoArguments().will(returnValue(0));
        emfConsole.expects(once()).method("height").withNoArguments().will(returnValue(0));

        ManagedView managedViewProxy = (ManagedView) managedView.proxy();
        managedView.expects(once()).method("getName").withNoArguments();
        managedView.expects(once()).method("width").withNoArguments().will(returnValue(0));
        //managedView.expects(once()).method("height").withNoArguments().will(returnValue(0));
        managedView.expects(once()).method("setPosition").withAnyArguments();

        windowsMenu.expects(once()).method("register").with(same(managedViewProxy));

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()),
                (EmfConsoleView) emfConsole.proxy());
        desktopManager.openWindow(managedViewProxy);
    }

    public void testShouldUnRegisterCloseWindowWithWindowMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);
        Mock managedView = mock(ManagedView.class);

        ManagedView managedViewProxy = (ManagedView) managedView.proxy();
        managedView.expects(once()).method("getName").withNoArguments();
        windowsMenu.expects(once()).method("unregister").with(same(managedViewProxy));

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()), null);
        desktopManager.closeWindow(managedViewProxy);
    }

    public void itestShouldCloseAllWindowsAndUnRegisterFromWindowMenu() {
        Mock managedView1 = manageView();
        Mock managedView2 = manageView();

        Mock windowsMenu = windoswMenu();

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()), null);
        desktopManager.openWindow((ManagedView) managedView1.proxy());
        desktopManager.openWindow((ManagedView) managedView2.proxy());

        desktopManager.closeAll();
    }

    private Mock windoswMenu() {
        Mock managedView = mock(ManagedView.class);

        Mock windowsMenu = mock(WindowMenuView.class);
        windowsMenu.expects(atLeastOnce()).method("register").with(eq(managedView.proxy()));
        windowsMenu.expects(atLeastOnce()).method("unregister").with(eq(managedView.proxy()));
        return windowsMenu;
    }

    private Mock manageView() {
        Mock managedView = mock(ManagedView.class);
        managedView.expects(atLeastOnce()).method("close").withNoArguments();
        managedView.expects(atLeastOnce()).method("getName").withNoArguments();
        return managedView;
    }

}