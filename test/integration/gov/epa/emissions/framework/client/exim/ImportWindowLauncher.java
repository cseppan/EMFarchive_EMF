package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.EmfConsolePresenter;
import gov.epa.emissions.framework.client.admin.UserServicesStub;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.jmock.core.stub.ReturnStub;

public class ImportWindowLauncher {

    public static void main(String[] args) throws EmfException {
        User user = createUser("joe", "Joe Fullman", "joef@zukoswky.com");
        UserServices userServices = createUserAdmin(user);
        
        Mock serviceLocator = new Mock(ServiceLocator.class);        
        serviceLocator.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getUserServices")).will(
                new ReturnStub(userServices));

        EmfConsole console = new EmfConsole(user, (ServiceLocator) serviceLocator.proxy());
        console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();

        console.display();
    }

    static private UserServices createUserAdmin(User user) throws EmfException {
        List users = new ArrayList();
        users.add(user);

        UserServices userAdmin = new UserServicesStub(users);

        return userAdmin;
    }

    static private User createUser(String username, String name, String email) throws EmfException {
        User user = new User();
        user.setUserName(username);
        user.setFullName(name);
        user.setEmailAddr(email);

        return user;
    }

}
