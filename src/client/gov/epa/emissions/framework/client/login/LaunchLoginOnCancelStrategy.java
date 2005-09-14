package gov.epa.emissions.framework.client.login;

import javax.swing.JFrame;

import gov.epa.emissions.framework.client.admin.RegisterCancelStrategy;
import gov.epa.emissions.framework.client.admin.RegisterUserPresenter;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

public class LaunchLoginOnCancelStrategy implements RegisterCancelStrategy {

    private ServiceLocator serviceLocator;

    public LaunchLoginOnCancelStrategy(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public void execute(RegisterUserPresenter presenter) {
        presenter.notifyCancel();

        launchLoginWindow();
    }

    private void launchLoginWindow() {
        LoginWindow login = new LoginWindow(serviceLocator);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter loginPresenter = new LoginPresenter(serviceLocator.getUserServices(), login);
        loginPresenter.observe();

        login.display();
    }

}
