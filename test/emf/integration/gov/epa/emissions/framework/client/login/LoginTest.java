package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTextField;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.matchers.NameMatcher;
import abbot.finder.matchers.WindowMatcher;
import abbot.tester.ComponentTester;

public class LoginTest extends ComponentTestFixture {

    private boolean isWindowClosed = false;

    public void testShouldCloseOnClickCancel() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        clickButton(window, "Cancel");

        assertTrue(isWindowClosed);
    }

    private void clickButton(LoginWindow window, String button) throws Exception {
        Component cancel = getFinder().find(window, new NameMatcher(button));
        ComponentTester tester = new ComponentTester();
        tester.actionClick(cancel);
    }

    public void testShouldShowEmfConsoleOnLogin() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        setUsername(window, "admin");
        setPassword(window, "admin123");

        clickButton(window, "Sign In");

        assertTrue(isWindowClosed);

        EmfConsole console = (EmfConsole) getFinder().find(new WindowMatcher("EMF Console"));
        assertNotNull(console);
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

        setUsername(window, "admin");
        setPassword(window, "invalid password");

        Component signIn = getFinder().find(window, new NameMatcher("Sign In"));
        ComponentTester tester = new ComponentTester();
        tester.actionClick(signIn);

        MessagePanel messagePanel = (MessagePanel) getFinder().find(window, new NameMatcher("MessagePanel"));
        assertEquals("Incorrect Password", messagePanel.getMessage());
    }

    private LoginWindow createLoginWindow() {
        ServiceLocator serviceLocator = new RemoteServiceLocator("http://localhost:8080/emf/services");

        LoginWindow window = new LoginWindow(serviceLocator);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(serviceLocator.getUserServices(), window);
        presenter.observe();
        return window;
    }

}
