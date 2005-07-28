package gov.epa.emissions.framework.client.admin;

import java.awt.Dimension;
import java.awt.Point;

import gov.epa.emissions.framework.client.EmfWidgetContainer;
import gov.epa.emissions.framework.client.EmfWindow;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

public class RegisterUserWindow extends EmfWindow implements EmfWidgetContainer {

    private RegisterUserPanel view;

    public RegisterUserWindow(EMFUserAdmin userAdmin, PostRegisterStrategy postRegisterStrategy) {
        view = new RegisterUserPanel(userAdmin, postRegisterStrategy, this);
        this.getContentPane().add(view);

        this.setTitle("Register New User");
        this.setSize(new Dimension(800, 600));
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
