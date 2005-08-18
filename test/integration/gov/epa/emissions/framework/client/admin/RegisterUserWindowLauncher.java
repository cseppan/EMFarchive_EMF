package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.login.LaunchEmfConsolePostRegisterStrategy;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.UserServices;

import java.util.Collections;

import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.jmock.core.stub.ReturnStub;

public class RegisterUserWindowLauncher {

    public static void main(String[] args) throws Exception {

        UserServices userServices = new UserServicesStub(Collections.EMPTY_LIST);
        Mock serviceLocator = new Mock(ServiceLocator.class);
        serviceLocator.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getUserServices")).will(
                new ReturnStub(userServices));

        PostRegisterStrategy strategy = new LaunchEmfConsolePostRegisterStrategy((ServiceLocator) serviceLocator
                .proxy());
        RegisterUserWindow window = new RegisterUserWindow(userServices, strategy);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        RegisterUserPresenter presenter = new RegisterUserPresenter(userServices, window.getView());
        presenter.observe();

        window.display();
    }

}
