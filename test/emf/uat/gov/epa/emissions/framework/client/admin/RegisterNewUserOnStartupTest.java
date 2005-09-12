package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import abbot.tester.ComponentTester;

public class RegisterNewUserOnStartupTest extends UserAcceptanceTestCase {

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
}
