package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.login.CreateUserWindow;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class CreateUserWindowLauncher {

    public static void main(String[] args) throws Exception {
        setLookAndFeel();

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

    private static void setLookAndFeel() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String nativeLF = UIManager.getSystemLookAndFeelClassName();
        UIManager.setLookAndFeel(nativeLF);
    }

}
