package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.stub.ReturnStub;
import org.jmock.core.stub.ThrowStub;

public class LoginWindowLauncher {  

    public static void main(String[] args) throws Exception {
        Mock userAdmin = new Mock(EMFUserAdmin.class);
        setFailureExpectation(userAdmin);
        setSuccessExpectation(userAdmin, "user", "user");        
        EMFUserAdmin userAdminProxy = (EMFUserAdmin) userAdmin.proxy();

        LoginWindow login = new LoginWindow(userAdminProxy);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(userAdminProxy, login);
        presenter.init();

        login.setVisible(true);
    }

    private static void setSuccessExpectation(Mock userAdmin, String username, String password) {
        userAdmin.stubs()
                 .method("authenticate")
                 .with(new IsEqual(username), new IsEqual(password), new IsEqual(Boolean.FALSE))
                 .will(new ReturnStub(null));
    }

    private static void setFailureExpectation(Mock userAdmin) {
        userAdmin.stubs()
                 .method("authenticate")
                 .withAnyArguments()
                 .will(new ThrowStub(new EmfException("invalid username/password")));
    }

}
