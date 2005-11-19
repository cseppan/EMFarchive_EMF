package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.login.LaunchEmfConsolePostRegisterStrategy;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.UserService;

import java.util.Collections;

import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.jmock.core.stub.ReturnStub;

public class RegisterUserWindowLauncher {

    public static void main(String[] args) throws Exception {
        UserService userServices = new UserServiceStub(Collections.EMPTY_LIST);
        Mock serviceLocator = new Mock(ServiceLocator.class);
        serviceLocator.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getUserServices")).will(
                new ReturnStub(userServices));

        ServiceLocator serviceLocatorProxy = (ServiceLocator) serviceLocator.proxy();
        PostRegisterStrategy strategy = new LaunchEmfConsolePostRegisterStrategy(serviceLocatorProxy);

        RegisterUserWindow view = new RegisterUserWindow(serviceLocatorProxy, strategy);
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        RegisterUserPresenter presenter = new RegisterUserPresenter(userServices);
        presenter.display(view);
    }

}
