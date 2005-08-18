package gov.epa.emissions.framework.client.login;


import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsAnything;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeOnceMatcher;
import org.jmock.core.stub.ReturnStub;
import org.jmock.core.stub.ThrowStub;

public class LoginWindowLauncher {

    public static void main(String[] args) throws UserException {
        Mock userAdmin = new Mock(UserServices.class);
        setFailureExpectation(userAdmin);
        setSuccessExpectation(userAdmin, "user", "user");
        UserServices userAdminProxy = (UserServices) userAdmin.proxy();

        LoginWindow login = new LoginWindow(userAdminProxy);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(userAdminProxy, login);
        presenter.observe();

        login.setVisible(true);
    }

    private static void setSuccessExpectation(Mock userAdmin, String username, String password) throws UserException {
        userAdmin.stubs().method("authenticate").with(new IsEqual(username), new IsEqual(password),
                new IsEqual(Boolean.FALSE));
        userAdmin.stubs().method("getUser").with(new IsEqual(username)).will(new ReturnStub(new User()));

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
