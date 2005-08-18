package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.admin.UserServicesStub;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.UserServices;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;

import javax.swing.JTextField;

import junit.extensions.abbot.ComponentTestFixture;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.jmock.core.stub.ReturnStub;

import abbot.finder.matchers.NameMatcher;
import abbot.tester.ComponentTester;

public class LoginTest extends ComponentTestFixture {

    private boolean isWindowClosed = false;

    public void testShouldCloseOnClickCancel() throws Exception {
        UserServicesStub userServices = new UserServicesStub(Collections.EMPTY_LIST);

        Mock serviceLocator = new Mock(ServiceLocator.class);
        serviceLocator.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getUserServices")).will(
                new ReturnStub(userServices));

        LoginWindow window = new LoginWindow((ServiceLocator) serviceLocator.proxy());
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
        ServiceLocator serviceLocator = new RemoteServiceLocator("http://ben.cep.unc.edu:8080/emf/services");

        UserServices userServices = serviceLocator.getUserServices();

        LoginWindow window = new LoginWindow((ServiceLocator) userServices);
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
