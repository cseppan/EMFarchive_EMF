package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.login.CreateUserWindow;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;

public class CreateUserWindowLauncher {

    public static void main(String[] args) throws EmfException {
        EMFUserAdmin createUserAdmin = new EMFUserAdminStub(Collections.EMPTY_LIST);

        CreateUserWindow window = new CreateUserWindow();
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });

        CreateUserPresenter presenter = new CreateUserPresenter(createUserAdmin, window);
        presenter.init();

        window.show();
    }

}
