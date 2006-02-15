package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.ChangeObserver;
import gov.epa.emissions.framework.client.DefaultChangeObserver;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.login.LaunchLoginOnCancelStrategy;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import java.awt.Point;

public class RegisterUserWindow extends EmfFrame implements RegisterUserView, ChangeObserver {

    private RegisterUserPanel view;

    private LaunchLoginOnCancelStrategy onCancelStrategy;

    private ChangeObserver changeObserver;

    public RegisterUserWindow(ServiceLocator serviceLocator, PostRegisterStrategy postRegisterStrategy) {
        super("RegisterUser", "Register New User");
        onCancelStrategy = new LaunchLoginOnCancelStrategy(serviceLocator);
        changeObserver = new DefaultChangeObserver(this);
        view = new RegisterUserPanel(postRegisterStrategy, onCancelStrategy, this, changeObserver);
        this.getContentPane().add(view);

        this.setSize(view.getSize());
        this.setLocation(new Point(400, 200));
        display();
    }

    protected void doClose() {
        RegisterUserPresenter presenter = view.getPresenter();
        onCancelStrategy.execute(presenter);
    }

    public void observe(RegisterUserPresenter presenter) {
        view.observe(presenter);
    }

    public void signalChanges() {
        changeObserver.signalChanges();
    }

    public void signalSaved() {
        changeObserver.signalSaved();
    }

}
