package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.UserService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UpdateUserPresenterTest extends MockObjectTestCase {

    private UpdateUserPresenter presenter;

    private Mock userServices;

    private Mock view;

    protected void setUp() {
        userServices = mock(UserService.class);
        view = mock(UpdateUserView.class);

        presenter = new UpdateUserPresenter((UserService) userServices.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();

        presenter.display((UpdateUserView) view.proxy());
    }

    public void testShouldUpdateUserViaEmfUserAdminOnNotifySave() throws Exception {
        User user = new User();
        user.setUsername("joey");
        user.setFullName("Joey Moey");

        userServices.expects(once()).method("updateUser").with(eq(user));

        presenter.doSave(user);
    }

    public void testShouldCloseViewOnCloseActionWithNoUserDataEdits() {
        view.expects(once()).method("close").withNoArguments();

        presenter.doClose();
    }

    public void testShouldConfirmLosingChangesOnCloseAfterEdits() {
        view.expects(once()).method("closeOnConfirmLosingChanges").withNoArguments();

        presenter.onChange();
        presenter.doClose();
    }

    public void testShouldCloseWithNoPromptsOnSaveFollowedByClose() throws Exception {
        User user = new User();
        user.setUsername("joey");
        user.setFullName("Joey Moey");

        userServices.expects(once()).method("updateUser").with(eq(user));

        view.expects(once()).method("close").withNoArguments();

        presenter.doSave(user);
        presenter.doClose();
    }

}
