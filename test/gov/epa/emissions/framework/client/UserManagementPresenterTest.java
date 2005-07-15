package gov.epa.emissions.framework.client;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UserManagementPresenterTest extends MockObjectTestCase {

    public void testShouldCloseViewOnClickOfCloseButton() {
        Mock view = mock(UsersManagementView.class);
        
        UsersManagementPresenter presenter = new UsersManagementPresenter((UsersManagementView) view.proxy());
        
        view.expects(once()).method("setViewObserver").with(eq(presenter));
        view.expects(once()).method("closeView").withNoArguments();
        
        presenter.init();        
        presenter.notifyCloseView();        
    }

}
