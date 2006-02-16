package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.ui.Position;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DesktopManagerTest extends MockObjectTestCase {

    public void testShouldRegisterOpenWindowWithWindowMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);
        Mock managedView = manageView("view");
        Mock emfConsole = emfConsole();

        windowsMenu.expects(once()).method("register").with(same(managedView.proxy()));

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()),
                (EmfConsoleView) emfConsole.proxy());
        desktopManager.openWindow((ManagedView) (managedView.proxy()));
    }

    public void testShouldUnRegisterCloseWindowWithWindowMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);
        Mock managedView = mock(ManagedView.class);

        ManagedView managedViewProxy = (ManagedView) managedView.proxy();
        managedView.expects(once()).method("getName").withNoArguments().will(returnValue("view"));
        windowsMenu.expects(once()).method("unregister").with(same(managedViewProxy));

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()), null);
        desktopManager.closeWindow(managedViewProxy);
    }

    public void testShouldCloseAllWindowsAndUnRegisterFromWindowMenu() {
        Mock managedView1 = manageView("view1");
        Mock managedView2 = manageView("view2");
        managedView1.expects(once()).method("getPosition").withNoArguments().will(returnValue(new Position(0, 0)));
        setCloseRealatedExpections(managedView1);
        setCloseRealatedExpections(managedView2);

        Mock windowsMenu = windowsMenu();
        Mock emfConsole = emfConsole();
        emfConsole.expects(atLeastOnce()).method("confirm").withNoArguments().will(returnValue(true));

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()),
                (EmfConsoleView) emfConsole.proxy());
        desktopManager.openWindow((ManagedView) managedView1.proxy());
        desktopManager.openWindow((ManagedView) managedView2.proxy());

        desktopManager.closeAll();
    }

    private void setCloseRealatedExpections(Mock managedView) {
        managedView.expects(once()).method("close").withNoArguments();
        managedView.expects(once()).method("hasChanges").withNoArguments().will(returnValue(false));
        managedView.expects(once()).method("forceClose").withNoArguments();
    }

    private Mock emfConsole() {
        Mock console = mock(EmfConsoleView.class);
        console.expects(atLeastOnce()).method("width").withNoArguments().will(returnValue(0));
        console.expects(atLeastOnce()).method("height").withNoArguments().will(returnValue(0));
        return console;
    }

    private Mock windowsMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);
        windowsMenu.expects(atLeastOnce()).method("register").with(isA(ManagedView.class));
        return windowsMenu;
    }

    private Mock manageView(String name) {
        Mock managedView = mock(ManagedView.class);
        managedView.expects(atLeastOnce()).method("getName").withNoArguments().will(returnValue(name));
        managedView.expects(atLeastOnce()).method("bringToFront").withNoArguments();
        managedView.expects(atLeastOnce()).method("width").withNoArguments().will(returnValue(0));
        managedView.expects(atLeastOnce()).method("height").withNoArguments().will(returnValue(0));
        managedView.expects(atLeastOnce()).method("setPosition").withAnyArguments();
        return managedView;
    }

}