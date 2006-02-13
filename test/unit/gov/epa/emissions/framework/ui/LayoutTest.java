package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsoleView;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LayoutTest extends MockObjectTestCase {

    public void testShouldAddOneViewAndLayoutTheView() {
        ManagedView manageView = manageViewProxy();
        EmfConsoleView emfConsoleView = emfConsoleProxy();
        Layout layout = new LayoutImpl(emfConsoleView);
        layout.add(manageView);
    }

    private ManagedView manageViewProxy() {
        Mock managedView = mock(ManagedView.class);
        managedView.expects(once()).method("width").withNoArguments().will(returnValue(200));
        managedView.expects(once()).method("height").withNoArguments().will(returnValue(200));
        managedView.expects(once()).method("setPosition").withAnyArguments();
        return (ManagedView) managedView.proxy();
    }

    private EmfConsoleView emfConsoleProxy() {
        Mock console = mock(EmfConsoleView.class);
        console.expects(once()).method("width").withNoArguments().will(returnValue(700));
        console.expects(once()).method("height").withNoArguments().will(returnValue(500));
        return (EmfConsoleView) console.proxy();
    }

    public void testShouldRemoveViewFromStackWhenRemoveIsCalled() {
        ManagedView manageView = manageViewProxy();
        
        EmfConsoleView emfConsoleView = emfConsoleProxy();
        
        Layout layout = new LayoutImpl(emfConsoleView);
        layout.add(manageView);
        layout.remove(manageView);
    }

}
