package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.admin.RegisterUserWindow;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import abbot.finder.matchers.NameMatcher;
import abbot.finder.matchers.WindowMatcher;
import abbot.tester.ComponentTester;

public class LoginTest extends UserAcceptanceTestCase {

    private boolean isWindowClosed = false;

    public void testShouldCloseOnClickCancel() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        click(window, "cancel");

        assertTrue(isWindowClosed);
    }

    public void testShouldShowEmfConsoleOnLogin() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        setUsername(window, "emf");
        setPassword(window, "emf12345");

        click(window, "signIn");

        assertTrue(isWindowClosed);
        assertEmfConsoleShown();
    }

    public void testShouldShowRegisterUserOnSelectionOfRegisterNewUserOption() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        click(window, "registerUser");

        assertTrue(isWindowClosed);

        RegisterUserWindow registerUser = (RegisterUserWindow) getFinder().find(new WindowMatcher("RegisterUser"));
        assertNotNull(registerUser);
        assertTrue(registerUser.isVisible());
    }

    private void setPassword(LoginWindow window, String password) throws Exception {
        setTextfield(window, "password", password);
    }

    private void setUsername(LoginWindow window, String username) throws Exception {
        setTextfield(window, "username", username);
    }

    public void testShouldShowErrorMessageOnInvalidUsername() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        setUsername(window, "invalid username");

        click(window, "signIn");

        assertErrorMessage(window, "Invalid username");
    }

    public void testShouldShowErrorMessageOnInvalidPassword() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        setUsername(window, "emf");
        setPassword(window, "invalid password");

        Component signIn = getFinder().find(window, new NameMatcher("signIn"));
        ComponentTester tester = new ComponentTester();
        tester.actionClick(signIn);

        assertErrorMessage(window, "Incorrect Password");
    }

}
