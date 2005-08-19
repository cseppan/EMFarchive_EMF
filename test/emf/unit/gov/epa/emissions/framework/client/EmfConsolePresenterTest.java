package gov.epa.emissions.framework.client;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class EmfConsolePresenterTest extends MockObjectTestCase {

    public void testShouldSetAsObserverOnObserve() {
        Mock view = mock(EmfConsoleView.class);         
        
        EmfConsolePresenter presenter = new EmfConsolePresenter((EmfConsoleView)view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));

        presenter.observe();
    }

    public void testShouldDisplayUserManagerOnNotifyManagerUsers() {
        Mock view = mock(EmfConsoleView.class);         
        view.expects(once()).method("displayUserManager").withNoArguments();
        
        EmfConsolePresenter presenter = new EmfConsolePresenter((EmfConsoleView)view.proxy());
        
        presenter.notifyManageUsers();
    }
}
