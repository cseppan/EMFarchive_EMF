package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.UserServices;
import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UpdateUserPresenterTest extends MockObjectTestCase {

    public void testShouldUpdateUserViaEmfUserAdminOnNotifySave() throws EmfException {
        User user = new User();
        user.setUserName("joey");
        user.setFullName("Joey Moey");

        Mock emfUserAdmin = mock(UserServices.class);
        emfUserAdmin.expects(once()).method("updateUser").with(eq(user));

        Mock view = mock(UpdateUserView.class);

        UpdateUserPresenter presenter = new UpdateUserPresenter((UserServices) emfUserAdmin.proxy(),
                (UpdateUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));
        presenter.observe();

        presenter.notifySave(user);
    }

    public void testShouldCloseViewOnCloseActionWithNoUserDataEdits() {
        Mock view = mock(UpdateUserView.class);
        view.expects(once()).method("close").withNoArguments();

        UpdateUserPresenter presenter = new UpdateUserPresenter(null, (UpdateUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));
        presenter.observe();

        presenter.notifyClose();
    }

    public void testShouldConfirmLosingChangesOnCloseAfterEdits() {
        Mock view = mock(UpdateUserView.class);
        view.expects(once()).method("closeOnConfirmLosingChanges").withNoArguments();
       
        UpdateUserPresenter presenter = new UpdateUserPresenter(null, (UpdateUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));
        presenter.observe();

        presenter.notifyChanges();
        presenter.notifyClose();
    }
    
    public void testShouldCloseWithNoPromptsOnSaveFollowedByClose() throws EmfException {
        User user = new User();
        user.setUserName("joey");
        user.setFullName("Joey Moey");

        Mock emfUserAdmin = mock(UserServices.class);
        emfUserAdmin.expects(once()).method("updateUser").with(eq(user));

        Mock view = mock(UpdateUserView.class);
        view.expects(once()).method("close").withNoArguments();
        
        UpdateUserPresenter presenter = new UpdateUserPresenter((UserServices) emfUserAdmin.proxy(),
                (UpdateUserView) view.proxy());
        view.expects(once()).method("setObserver").with(eq(presenter));
        presenter.observe();

        presenter.notifySave(user);
        presenter.notifyClose();
    }
    
}
