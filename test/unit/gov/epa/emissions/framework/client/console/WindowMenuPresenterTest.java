package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class WindowMenuPresenterTest extends MockObjectTestCase {

    private WindowMenuPresenter presenter;

    private ManagedView managedViewProxy;

    private Mock view;

    private Mock managedView;

    protected void setUp() {
        view = mock(WindowMenuView.class);
        presenter = new WindowMenuPresenter((WindowMenuView) view.proxy());
        managedView = mock(ManagedView.class);
        managedViewProxy = (ManagedView) managedView.proxy();
    }

    public void testShouldRegisterWindowWithMenuOnBeingAddedToDesktop() {
        view.expects(once()).method("register").with(same(managedViewProxy));
        presenter.notifyAdd(managedViewProxy);
    }

    public void testShouldUnregisterWindowWithMenuOnBeingRemovedFromDesktop() {
        view.expects(once()).method("unregister").with(same(managedViewProxy));
        presenter.notifyRemove(managedViewProxy);
    }

    public void testShouldBringManagedViewToFrontOnBeingSelectedFromMenu() {
        managedView.expects(once()).method("bringToFront").withNoArguments();
        presenter.select(managedViewProxy);
    }
}
