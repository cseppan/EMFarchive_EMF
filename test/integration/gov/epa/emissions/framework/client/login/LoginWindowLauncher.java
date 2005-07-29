package gov.epa.emissions.framework.client.login;


import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsAnything;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeOnceMatcher;
import org.jmock.core.stub.ThrowStub;

public class LoginWindowLauncher {

    public static void main(String[] args) throws UserException {
        Mock userAdmin = new Mock(EMFUserAdmin.class);
        setFailureExpectation(userAdmin);
        setSuccessExpectation(userAdmin, "user", "user");
        EMFUserAdmin userAdminProxy = (EMFUserAdmin) userAdmin.proxy();

        LoginWindow login = new LoginWindow(userAdminProxy);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(userAdminProxy, login);
        presenter.observe();

        login.setVisible(true);
    }

    private static void setSuccessExpectation(Mock userAdmin, String username, String password) throws UserException {
        userAdmin.stubs().method("authenticate").with(new IsEqual(username), new IsEqual(password),
                new IsEqual(Boolean.FALSE));

        User user = new User();
        user.setUserName("newuser");

        userAdmin.expects(new InvokeOnceMatcher()).method("createUser").with(new IsEqual(user));
        userAdmin.stubs().method("authenticate").with(new IsEqual("newuser"), new IsAnything(),
                new IsEqual(Boolean.FALSE));

    }

    private static void setFailureExpectation(Mock userAdmin) {
        userAdmin.stubs().method("authenticate").withAnyArguments().will(
                new ThrowStub(new EmfException("invalid username/password")));
    }

}
