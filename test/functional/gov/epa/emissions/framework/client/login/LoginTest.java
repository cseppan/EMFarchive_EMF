package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.admin.EMFUserAdminStub;
import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;
import gov.epa.emissions.framework.commons.EMFUserAdmin;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;

import javax.swing.JTextField;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.matchers.NameMatcher;
import abbot.tester.ComponentTester;

public class LoginTest extends ComponentTestFixture {

    private boolean isWindowClosed = false;

    public void testShouldCloseOnClickCancel() throws Exception {
        LoginWindow window = new LoginWindow(new EMFUserAdminStub(Collections.EMPTY_LIST));
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        Component cancel = getFinder().find(window, new NameMatcher("cancel"));

        ComponentTester tester = new ComponentTester();
        tester.actionClick(cancel);

        assertTrue(isWindowClosed);
    }

    public void testShouldShowEmfConsoleOnLogin() throws Exception {
        EMFUserAdmin emfUserAdmin = new EMFUserAdminTransport(
                "http://localhost:8080/emf/services/EMFUserManagerService");

        LoginWindow window = new LoginWindow(emfUserAdmin);
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        Component signIn = getFinder().find(window, new NameMatcher("signIn"));
        JTextField username = (JTextField) getFinder().find(window, new NameMatcher("username"));
        username.setText("admin");
        JTextField password = (JTextField) getFinder().find(window, new NameMatcher("password"));
        password.setText("admin123");

        ComponentTester tester = new ComponentTester();
        tester.actionClick(signIn);

        assertTrue(isWindowClosed);
    }

}
