package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UpdateUserPresenterTest extends MockObjectTestCase {

    public void testShouldUpdateUserViaEmfUserAdminOnNotifyUpdate() throws EmfException {
        User user = new User();
        user.setUserName("joey");
        user.setFullName("Joey Moey");

        Mock emfUserAdmin = mock(EMFUserAdmin.class);
        emfUserAdmin.expects(once()).method("updateUser").with(eq(user));

        Mock view = mock(UpdateUserView.class);

        UpdateUserPresenter presenter = new UpdateUserPresenter((EMFUserAdmin) emfUserAdmin.proxy(),
                (UpdateUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));
        presenter.observe();

        presenter.notifyUpdate(user);
    }
    
    public void testShouldCloseViewOnCancelAction() {
        Mock view = mock(UpdateUserView.class);
        view.expects(once()).method("close").withNoArguments();

        UpdateUserPresenter presenter = new UpdateUserPresenter(null, (UpdateUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));        
        presenter.observe();

        presenter.notifyCancel();
    }

}
