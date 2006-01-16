package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.login.LaunchLoginOnCancelStrategy;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import java.awt.Point;

public class RegisterUserWindow extends EmfFrame implements RegisterUserView {

    private RegisterUserPanel view;

    private LaunchLoginOnCancelStrategy onCancelStrategy;

    public RegisterUserWindow(ServiceLocator serviceLocator, PostRegisterStrategy postRegisterStrategy) {
        super("RegisterUser", "Register New User");
        onCancelStrategy = new LaunchLoginOnCancelStrategy(serviceLocator);
        view = new RegisterUserPanel(postRegisterStrategy, onCancelStrategy, this);
        this.getContentPane().add(view);

        this.setSize(view.getSize());
        this.setLocation(new Point(400, 200));
        display();
    }

    protected void doClose() {
        RegisterUserPresenter presenter = view.getPresenter();
        if (presenter != null)
            onCancelStrategy.execute(presenter);
    }

    public void observe(RegisterUserPresenter presenter) {
        view.observe(presenter);
    }

}
