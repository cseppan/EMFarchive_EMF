package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.ComponentNotFoundException;
import abbot.finder.MultipleComponentsFoundException;
import abbot.finder.matchers.NameMatcher;
import abbot.finder.matchers.WindowMatcher;
import abbot.tester.ComponentTester;

public class RegisterNewUserOnStartupTest extends ComponentTestFixture {

    public void testShouldShowRegisterUserOnSelectionOfRegisterNewUserOption() throws Exception {
        RegisterUserWindow window = gotoStart();

        assertNotNull(window);
        assertTrue(window.isVisible());
        assertEquals("Register New User", window.getTitle());
    }

    private boolean isWindowClosed = false;

    public void testShouldShowLoginOnCancel() throws Exception {
        RegisterUserWindow window = gotoStart();

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        click(window, "Cancel");

        assertTrue("Register window should close on cancel", isWindowClosed);
        
        assertLoginIsShown();
    }

    private void assertLoginIsShown() throws ComponentNotFoundException, MultipleComponentsFoundException {
        LoginWindow login = (LoginWindow) getFinder().find(new WindowMatcher("Login"));
        assertNotNull(login);
        assertTrue(login.isVisible());
    }

    public void TODO_testShouldShowLoginOnWindowClose() throws Exception {
        RegisterUserWindow window = gotoStart();

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        close(window);

        assertTrue("Register window should close on closing of window", isWindowClosed);
        assertLoginIsShown();
    }

    private void close(RegisterUserWindow window) {
        ComponentTester tester = new ComponentTester();
        tester.close(window);
    }

    private RegisterUserWindow gotoStart() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        click(window, "RegisterUser");

        return (RegisterUserWindow) getFinder().find(new WindowMatcher("RegisterUser"));
    }

    private void click(Container window, String componentName) throws Exception {
        Component registerLabel = getFinder().find(window, new NameMatcher(componentName));
        ComponentTester tester = new ComponentTester();

        tester.actionClick(registerLabel);
    }

    // FIXME: common code chunks - setup. Needs to be refactored
    private LoginWindow createLoginWindow() {
        ServiceLocator serviceLocator = new RemoteServiceLocator("http://localhost:8080/emf/services");

        LoginWindow window = new LoginWindow(serviceLocator);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(serviceLocator.getUserServices(), window);
        presenter.observe();

        assertEquals("Login to EMF", window.getTitle());

        return window;
    }
}
