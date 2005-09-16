package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.login.LaunchLoginOnCancelStrategy;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RegisterUserWindow extends EmfFrame implements EmfView {

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

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                doClose();
                super.windowClosing(event);
            }
        });
    }

    public void close() {
        super.dispose();
    }

    private void doClose() {
        RegisterUserPresenter presenter = view.getPresenter();
        if (presenter != null)
            onCancelStrategy.execute(presenter);
    }

    public RegisterUserView getView() {
        return view;
    }

    public void display() {
        this.setVisible(true);
    }

}
