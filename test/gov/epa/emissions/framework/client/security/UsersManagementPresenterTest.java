package gov.epa.emissions.framework.client.security;

import gov.epa.emissions.framework.commons.EMFUserAdmin;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UsersManagementPresenterTest extends MockObjectTestCase {

    public void testShouldCloseViewOnClickOfCloseButton() {
        Mock view = mock(UsersManagementView.class);

        UsersManagementPresenter presenter = new UsersManagementPresenter(null, (UsersManagementView) view.proxy());

        view.expects(once()).method("setViewObserver").with(eq(presenter));
        view.expects(once()).method("close").withNoArguments();

        presenter.init();
        presenter.notifyCloseView();
    }

    public void testShouldDeleteUserViaEMFUserAdminOnNotifyDelete() {
        Mock emfUserAdmin = mock(EMFUserAdmin.class);
        emfUserAdmin.expects(once()).method("deleteUser").with(eq("matts"));
        
        UsersManagementPresenter presenter = new UsersManagementPresenter((EMFUserAdmin)emfUserAdmin.proxy(), null);
        
        presenter.notifyDelete("matts");
    }
}
