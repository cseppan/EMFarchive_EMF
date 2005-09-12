package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.admin.RegisterUserWindow;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JTextField;

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

        click(window, "Cancel");

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

        click(window, "Sign In");

        assertTrue(isWindowClosed);

        EmfConsole console = (EmfConsole) getFinder().find(new WindowMatcher("EMF Console"));
        assertNotNull(console);
        assertTrue(console.isVisible());
    }

    public void testShouldShowRegisterUserOnSelectionOfRegisterNewUserOption() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        Component register = getFinder().find(window, new NameMatcher("RegisterUser"));
        ComponentTester tester = new ComponentTester();
        tester.actionClick(register);

        assertTrue(isWindowClosed);

        RegisterUserWindow registerUser = (RegisterUserWindow) getFinder().find(new WindowMatcher("RegisterUser"));
        assertNotNull(registerUser);
        assertTrue(registerUser.isVisible());
    }

    private void setPassword(LoginWindow window, String password) throws Exception {
        JTextField field = (JTextField) getFinder().find(window, new NameMatcher("password"));
        field.setText(password);
    }

    private void setUsername(LoginWindow window, String username) throws Exception {
        JTextField field = (JTextField) getFinder().find(window, new NameMatcher("username"));
        field.setText(username);
    }

    public void testShouldShowErrorMessageOnInvalidUsername() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        setUsername(window, "invalid username");

        Component signIn = getFinder().find(window, new NameMatcher("Sign In"));
        ComponentTester tester = new ComponentTester();
        tester.actionClick(signIn);

        MessagePanel messagePanel = (MessagePanel) getFinder().find(window, new NameMatcher("MessagePanel"));
        assertEquals("Invalid username", messagePanel.getMessage());
    }

    public void testShouldShowErrorMessageOnInvalidPassword() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        setUsername(window, "emf");
        setPassword(window, "invalid password");

        Component signIn = getFinder().find(window, new NameMatcher("Sign In"));
        ComponentTester tester = new ComponentTester();
        tester.actionClick(signIn);

        MessagePanel messagePanel = (MessagePanel) getFinder().find(window, new NameMatcher("MessagePanel"));
        assertEquals("Incorrect Password", messagePanel.getMessage());
    }

}
