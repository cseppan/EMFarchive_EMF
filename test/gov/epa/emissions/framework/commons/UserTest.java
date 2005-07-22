package gov.epa.emissions.framework.commons;

import gov.epa.emissions.framework.UserException;
import junit.framework.TestCase;

public class UserTest extends TestCase {

    public void testShouldFailIfUsernameIsLessThanThreeCharacters() {
        User user = new User();
        try {
            user.setUserName("a");
            user.setUserName("");
            user.setUserName("ab");
        } catch (UserException ex) {
            assertEquals("Username must have at least 3 characters", ex.getMessage());
            return;
        }

        fail("should have thrown an exception if username is less than 3 characters");
    }
}
