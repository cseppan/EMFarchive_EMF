package gov.epa.emissions.framework.commons;

import gov.epa.emissions.framework.UserException;
import junit.framework.TestCase;

public class UserTest extends TestCase {

    public void testShouldFailIfUsernameIsLessThanThreeCharacters() throws UserException {
        assertInvalidUsername("a");
        assertInvalidUsername("ab");
        assertInvalidUsername("");
        try {
            new User(null, null, null, null, "a2", null, false, false);
        } catch (UserException ex) {
            assertEquals("Username must have at least 3 characters", ex.getMessage());
            return;
        }

        fail("should have thrown an exception if username is less than 3 characters");

        User user = new User();
        user.setUserName("abcd");
        user.setUserName("abcd");
        new User("ab62", null, null, null, null, null, false, false);
    }

    private void assertInvalidUsername(String username) {
        User user = new User();
        try {
            user.setUserName(username);
        } catch (UserException ex) {
            assertEquals("Username must have at least 3 characters", ex.getMessage());
            return;
        }

        fail("should have thrown an exception if username is less than 3 characters");
    }

    public void testShouldFailIfPasswordSizeIsLessThanEightCharacters() throws UserException {
        assertInvalidPassword("");
        assertInvalidPassword("a");
        assertInvalidPassword("1234567");
        
        try {
            new User(null, null, null, null, "abc", "1234567", false, false);
        } catch (UserException ex) {
            assertEquals("Password must have at least 8 characters", ex.getMessage());
            return;
        }

        fail("should specify password to be atleast 8 characters in lengh");
        
        User user = new User();
        user.setPassword("12345678");
    }

    private void assertInvalidPassword(String password) {
        User user = new User();
        try {
            user.setPassword(password);
        } catch (UserException ex) {
            assertEquals("Password must have at least 8 characters", ex.getMessage());
            return;
        }

        fail("should specify password to be atleast 8 characters in lengh");
    }
}
