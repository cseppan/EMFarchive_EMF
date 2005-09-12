package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

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

        click(window, "cancel");

        assertTrue("Register window should close on cancel", isWindowClosed);

        assertLoginIsShown();
    }

    public void testShouldShowErrorsOnMissingName() throws Exception {
        RegisterUserWindow window = gotoStart();

        click(window, "ok");

        assertErrorMessage(window, "Name should be specified");
    }

    public void testShouldShowErrorsOnMissingAffiliation() throws Exception {
        RegisterUserWindow window = gotoStart();

        setTextfield(window, "name", "A User");

        click(window, "ok");

        assertErrorMessage(window, "Affiliation should have 2 or more characters");
    }

    public void testShouldShowErrorsOnMissingPhone() throws Exception {
        RegisterUserWindow window = gotoStart();

        setTextfield(window, "name", "A User");
        setTextfield(window, "affiliation", "User's affiliation");

        click(window, "ok");

        assertErrorMessage(window, "Phone should be specified");
    }

    public void testShouldShowErrorsOnMissingEmail() throws Exception {
        RegisterUserWindow window = gotoStart();

        setTextfield(window, "name", "A User");
        setTextfield(window, "affiliation", "User's affiliation");
        setTextfield(window, "phone", "123-123-1234-123");

        click(window, "ok");

        assertErrorMessage(window, "Email should have the format xx@yy.zz");
    }

    public void testShouldShowErrorsOnMissingUsername() throws Exception {
        RegisterUserWindow window = gotoStart();

        setTextfield(window, "name", "A User");
        setTextfield(window, "affiliation", "User's affiliation");
        setTextfield(window, "phone", "123-123-1234-123");
        setTextfield(window, "email", "uat-user@uat.emf.edu");

        click(window, "ok");

        assertErrorMessage(window, "Username should have at least 3 characters");
    }

    public void testShouldShowErrorsOnInvalidUsername() throws Exception {
        RegisterUserWindow window = gotoStart();

        setTextfield(window, "name", "A User");
        setTextfield(window, "affiliation", "User's affiliation");
        setTextfield(window, "phone", "123-123-1234-123");
        setTextfield(window, "email", "uat-user@uat.emf.edu");
        setTextfield(window, "username", "ua");

        click(window, "ok");

        assertErrorMessage(window, "Username should have at least 3 characters");
    }

    public void testShouldShowErrorsOnMissingPassword() throws Exception {
        RegisterUserWindow window = gotoStart();

        setTextfield(window, "name", "A User");
        setTextfield(window, "affiliation", "User's affiliation");
        setTextfield(window, "phone", "123-123-1234-123");
        setTextfield(window, "email", "uat-user@uat.emf.edu");
        setTextfield(window, "username", "uat-emf");

        click(window, "ok");

        assertErrorMessage(window, "Password should have at least 8 characters");
    }

    public void testShouldShowErrorsOnPasswordNotMatchingConfirmPassword() throws Exception {
        RegisterUserWindow window = gotoStart();

        setTextfield(window, "name", "A User");
        setTextfield(window, "affiliation", "User's affiliation");
        setTextfield(window, "phone", "123-123-1234-123");
        setTextfield(window, "email", "uat-user@uat.emf.edu");
        setTextfield(window, "username", "uat-emf");
        setTextfield(window, "password", "uatemf12");

        click(window, "ok");

        assertErrorMessage(window, "Confirm Password should match Password");
    }

    public void testShouldShowEmfConsoleOnSuccessfulRegistration() throws Exception {
        RegisterUserWindow window = gotoStart();

        setTextfield(window, "name", "A User");
        setTextfield(window, "affiliation", "User's affiliation");
        setTextfield(window, "phone", "123-123-1234");
        setTextfield(window, "email", "uat-user@uat.emf.edu");
        setTextfield(window, "username", "uat" + new Random().nextInt());
        setTextfield(window, "password", "uatemf12");
        setTextfield(window, "confirmPassword", "uatemf12");

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        click(window, "ok");

        assertTrue(isWindowClosed);
        assertEmfConsoleShown();
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
