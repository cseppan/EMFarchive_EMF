package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.UserManagerPresenter;
import gov.epa.emissions.framework.client.admin.UsersManagementView;
import gov.epa.emissions.framework.commons.UserServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UsersManagementPresenterTest extends MockObjectTestCase {

    public void testShouldCloseViewOnClickOfCloseButton() {
        Mock view = mock(UsersManagementView.class);

        UserManagerPresenter presenter = new UserManagerPresenter(null, (UsersManagementView) view.proxy());

        view.expects(once()).method("setViewObserver").with(eq(presenter));
        view.expects(once()).method("close").withNoArguments();

        presenter.observe();
        presenter.notifyCloseView();
    }

    public void testShouldDeleteUserViaEMFUserAdminOnNotifyDelete() throws EmfException {
        Mock emfUserAdmin = mock(UserServices.class);
        emfUserAdmin.expects(once()).method("deleteUser").with(eq("matts"));
        
        Mock view = mock(UsersManagementView.class);
        view.expects(once()).method("refresh").withNoArguments();
        
        UserManagerPresenter presenter = 
            new UserManagerPresenter((UserServices)emfUserAdmin.proxy(), (UsersManagementView) view.proxy());
        
        presenter.notifyDelete("matts");
    }
}
