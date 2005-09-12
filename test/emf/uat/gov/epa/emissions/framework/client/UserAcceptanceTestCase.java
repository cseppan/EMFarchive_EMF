package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.admin.RegisterUserWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JTextField;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.ComponentNotFoundException;
import abbot.finder.MultipleComponentsFoundException;
import abbot.finder.matchers.NameMatcher;
import abbot.finder.matchers.WindowMatcher;
import abbot.tester.ComponentTester;

public abstract class UserAcceptanceTestCase extends ComponentTestFixture {

    protected void assertLoginIsShown() throws Exception {
        LoginWindow login = (LoginWindow) getFinder().find(new WindowMatcher("Login"));
        assertNotNull(login);
        assertTrue(login.isVisible());
    }

    protected RegisterUserWindow gotoStart() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        click(window, "registerUser");

        return (RegisterUserWindow) getFinder().find(new WindowMatcher("RegisterUser"));
    }

    protected void click(Container window, String componentName) throws Exception {
        Component registerLabel = getFinder().find(window, new NameMatcher(componentName));
        ComponentTester tester = new ComponentTester();

        tester.actionClick(registerLabel);
    }

    protected LoginWindow createLoginWindow() {
        ServiceLocator serviceLocator = new RemoteServiceLocator("http://localhost:8080/emf/services");

        LoginWindow window = new LoginWindow(serviceLocator);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(serviceLocator.getUserServices(), window);
        presenter.observe();

        assertEquals("Login to EMF", window.getTitle());

        return window;
    }

    protected void setTextfield(Container window, String name, String value) throws Exception {
        JTextField field = (JTextField) getFinder().find(window, new NameMatcher(name));
        field.setText(value);
    }

    protected void assertErrorMessage(Container window, String errorMessage) throws Exception {
        MessagePanel messagePanel = (MessagePanel) getFinder().find(window, new NameMatcher("messagePanel"));
        assertEquals(errorMessage, messagePanel.getMessage());
    }

    protected void assertEmfConsoleShown() throws ComponentNotFoundException, MultipleComponentsFoundException {
        EmfConsole console = (EmfConsole) getFinder().find(new WindowMatcher("EMF Console"));
        assertNotNull(console);
        assertTrue(console.isVisible());
    }

}
