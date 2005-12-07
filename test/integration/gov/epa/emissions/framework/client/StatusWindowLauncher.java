package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.admin.UserServiceStub;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.console.EmfConsolePresenter;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;
import gov.epa.emissions.framework.services.UserService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.jmock.core.stub.ReturnStub;

public class StatusWindowLauncher {

    public static void main(String[] args) throws Exception {
        User user = createUser("joe", "Joe Fullman", "joef@zukoswky.com");
        UserService userServices = createUserServices(user);

        StatusService statusServices = createStatusServices(user);

        Mock serviceLocator = new Mock(ServiceLocator.class);
        serviceLocator.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getUserServices")).will(
                new ReturnStub(userServices));
        serviceLocator.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getStatusServices")).will(
                new ReturnStub(statusServices));

        EmfConsole view = new EmfConsole(new DefaultEmfSession(user, (ServiceLocator) serviceLocator.proxy()));
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EmfConsolePresenter presenter = new EmfConsolePresenter();
        presenter.display(view);
    }

    private static StatusService createStatusServices(User user) {
        Status nonRoad = new Status("user1", "type1", "message1", new Date());
        Status onRoad = new Status("user2", "type2", "message2", new Date());
        Status nonPoint = new Status("user3", "type3", "message3", new Date());

        Status[] statuses = new Status[] { nonRoad, onRoad, nonPoint };

        Mock service = new Mock(StatusService.class);
        service.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getMessages")).with(
                new IsEqual(user.getUsername())).will(new ReturnStub(statuses));

        return (StatusService) service.proxy();
    }

    static private UserService createUserServices(User user) {
        List users = new ArrayList();
        users.add(user);

        return new UserServiceStub(users);
    }

    static private User createUser(String username, String name, String email) throws Exception {
        User user = new User();
        user.setUsername(username);
        user.setFullName(name);
        user.setEmail(email);

        return user;
    }

}
