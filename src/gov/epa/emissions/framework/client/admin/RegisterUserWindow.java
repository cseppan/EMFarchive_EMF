package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.client.EmfWindow;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.Point;

public class RegisterUserWindow extends EmfWindow implements EmfWidgetContainer {

    private RegisterUserPanel view;

    //TODO: should the app exit if 'x-close' is clicked ?
    public RegisterUserWindow(UserServices userAdmin, PostRegisterStrategy postRegisterStrategy) {
        view = new RegisterUserPanel(userAdmin, postRegisterStrategy, this);
        this.getContentPane().add(view);

        this.setTitle("Register New User");
        this.setSize(view.getSize());
        this.setLocation(new Point(400, 200));
        this.setVisible(true);        
    }

    public void close() {
        this.dispose();
    }

    public RegisterUserView getView() {
        return view;
    }

    public void display() {
        this.setVisible(true);
    }

}
