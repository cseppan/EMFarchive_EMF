package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.login.LaunchEmfConsolePostRegisterStrategy;
import gov.epa.emissions.framework.commons.UserServices;

import java.util.Collections;

import javax.swing.JFrame;

public class RegisterUserWindowLauncher {

    public static void main(String[] args) throws Exception {
        UserServices userAdmin = new EMFUserAdminStub(Collections.EMPTY_LIST);

        PostRegisterStrategy strategy = new LaunchEmfConsolePostRegisterStrategy(userAdmin);
        RegisterUserWindow window = new RegisterUserWindow(userAdmin, strategy);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        

        RegisterUserPresenter presenter = new RegisterUserPresenter(userAdmin, window.getView());
        presenter.observe();
        
        window.display();
    }

}
