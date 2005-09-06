package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.admin.UserServicesStub;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.jmock.core.stub.ReturnStub;

public class StatusWindowLauncher {

    public static void main(String[] args) throws EmfException {
        User user = createUser("joe", "Joe Fullman", "joef@zukoswky.com");
        UserServices userServices = createUserServices(user);

        StatusServices statusServices = createStatusServices(user);

        Mock serviceLocator = new Mock(ServiceLocator.class);
        serviceLocator.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getUserServices")).will(
                new ReturnStub(userServices));
        serviceLocator.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getStatusServices")).will(
                new ReturnStub(statusServices));

        EmfConsole console = new EmfConsole(new EmfSession(user, (ServiceLocator) serviceLocator.proxy()));
        console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EmfConsolePresenter presenter = new EmfConsolePresenter(console);
        presenter.observe();

        console.display();
    }

    private static StatusServices createStatusServices(User user) {
        Status nonRoad = new Status("user1", "type1", "message1", new Date());
        Status onRoad = new Status("user2", "type2", "message2", new Date());
        Status nonPoint = new Status("user3", "type3", "message3", new Date());

        Status[] statuses = new Status[] { nonRoad, onRoad, nonPoint };

        Mock service = new Mock(StatusServices.class);
        service.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getMessages")).with(
                new IsEqual(user.getUserName())).will(new ReturnStub(statuses));

        return (StatusServices) service.proxy();
    }

    static private UserServices createUserServices(User user) throws EmfException {
        List users = new ArrayList();
        users.add(user);

        return new UserServicesStub(users);
    }

    static private User createUser(String username, String name, String email) throws EmfException {
        User user = new User();
        user.setUserName(username);
        user.setFullName(name);
        user.setEmailAddr(email);

        return user;
    }

}
