package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.admin.RegisterUserWindow;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.status.StatusWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.Matcher;
import abbot.finder.matchers.NameMatcher;
import abbot.finder.matchers.WindowMatcher;
import abbot.tester.ComponentTester;
import abbot.tester.JComboBoxTester;

//TODO: reorganize EMF-specific vs Abbot-specific methods
public abstract class UserAcceptanceTestCase extends ComponentTestFixture {

    protected RegisterUserWindow gotoRegisterNewUserScreen() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        click(window, "registerUser");

        return (RegisterUserWindow) findWindow("RegisterUser");
    }

    protected void click(Container window, String componentName) throws Exception {
        Component component = findByName(window, componentName);
        ComponentTester tester = new ComponentTester();

        tester.actionClick(component);
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
        JTextField field = (JTextField) findByName(window, name);
        field.setText(value);
    }

    protected void assertErrorMessage(Container window, String errorMessage) throws Exception {
        MessagePanel messagePanel = (MessagePanel) findByName(window, "messagePanel");
        assertEquals(errorMessage, messagePanel.getMessage());
    }

    protected Component findByName(Container window, String componentName) throws Exception {
        return getFinder().find(window, new NameMatcher(componentName));
    }

    protected void assertEmfConsoleShown() throws Exception {
        EmfConsole console = (EmfConsole) findWindow("EMF Console");
        assertNotNull(console);
        assertTrue(console.isVisible());
    }

    protected EmfConsole gotoConsole() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        setUsername(window, "emf");
        setPassword(window, "emf12345");

        click(window, "signIn");

        return (EmfConsole) findWindow("EMF Console");
    }

    private void setPassword(LoginWindow window, String password) throws Exception {
        setTextfield(window, "password", password);
    }

    private void setUsername(LoginWindow window, String username) throws Exception {
        setTextfield(window, "username", username);
    }

    protected Component findWindow(String title) throws Exception {
        return getFinder().find(new WindowMatcher(title));
    }

    protected JInternalFrame findInternalFrame(JFrame frame, final String name) throws Exception {
        return (JInternalFrame) getFinder().find(frame, new Matcher() {
            public boolean matches(Component component) {
                if (!(component instanceof JInternalFrame))
                    return false;

                JInternalFrame internalFrame = (JInternalFrame) component;
                return name.equals(internalFrame.getName());
            }
        });
    }

    protected JComboBox findComboBox(Container container, final String name) throws Exception {
        return (JComboBox) getFinder().find(container, new Matcher() {
            public boolean matches(Component component) {
                if (!(component instanceof JComboBox))
                    return false;
    
                JComboBox comboBox = (JComboBox) component;
                return name.equals(comboBox.getName());
            }
        });
    }

    protected void selectComboBoxItem(ImportWindow window, String comboBoxName, String value) throws Exception {
        JComboBox comboBox = findComboBox(window, comboBoxName);
        JComboBoxTester tester = new JComboBoxTester();
        tester.actionSelectItem(comboBox, value);
    }

    protected StatusWindow getStatusWindow(EmfConsole window) throws Exception {
        return (StatusWindow) findInternalFrame(window, "status");
    }

}
