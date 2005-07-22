package gov.epa.emissions.framework.commons;

import gov.epa.emissions.framework.UserException;
import junit.framework.TestCase;

public class UserTest extends TestCase {

    public void testShouldFailIfUsernameIsLessThanThreeCharacters() throws UserException {
        assertInvalidUsername("a");
        assertInvalidUsername("ab");
        assertInvalidUsername("");
        try {
            new User(null, "abc", null, null, "a2", null, false, false);
        } catch (UserException ex) {
            assertEquals("Username must have at least 3 characters", ex.getMessage());
            return;
        }

        fail("should fail if username is less than 3 characters");
    }

    public void testShouldAllowUsernameIfSizeIsGreaterThan2CharactersOnConstruction() throws UserException {
        new User(null, "abc", null, null, "ab62", "abcd1234", false, false);
    }

    public void testShouldAllowUsernameIfSizeIsGreaterThan2Characters() throws UserException {
        User user = new User();
        user.setUserName("abcd");
    }

    private void assertInvalidUsername(String username) {
        User user = new User();
        try {
            user.setUserName(username);
        } catch (UserException ex) {
            assertEquals("Username must have at least 3 characters", ex.getMessage());
            return;
        }

        fail("should fail if username is less than 3 characters");
    }

    public void testShouldFailIfPasswordSizeIsLessThanEightCharacters() throws UserException {
        assertInvalidPasswordDueToSize("");
        assertInvalidPasswordDueToSize("a");
        assertInvalidPasswordDueToSize("1234567");

        try {
            new User(null, "abc", null, null, "abc", "1234567", false, false);
        } catch (UserException ex) {
            assertEquals("Password must have at least 8 characters", ex.getMessage());
            return;
        }

        fail("should fail when password is less than 8 characters in lengh");
    }

    public void testShouldAllowPasswordsOfLengthGreaterThan8StartingWithAlphabetAndContainingAtleastOneDigit()
            throws UserException {
        User user = new User();
        user.setPassword("as12345678");
    }

    public void testShouldFailIfPasswordDoesNotHaveAtleastOneNonAlphabeticCharacter() throws UserException {
        assertPasswordInvalidOnContentRulesFailure("abcdefgh");
        assertPasswordInvalidOnContentRulesFailure("12asd454564");
    }

    public void testShouldFailIfUsernameMatchesPasswordOnSetPassword() throws UserException {
        User user = new User();
        user.setUserName("abcdefg1");

        try {
            user.setPassword("abcdefg1");
        } catch (UserException ex) {
            assertEquals("Username must be different from Password", ex.getMessage());
            return;
        }

        fail("should fail if password matches username");
    }

    public void testShouldFailIfUsernameMatchesPasswordOnSetUsername() throws UserException {
        User user = new User();
        user.setPassword("abcdefg1");

        try {
            user.setUserName("abcdefg1");
        } catch (UserException ex) {
            assertEquals("Username must be different from Password", ex.getMessage());
            return;
        }

        fail("should fail if password matches username");
    }

    public void testShouldFailIfUsernameMatchesPasswordOnConstruction() throws UserException {
        try {
            new User(null, "abd", null, null, "abcd1234", "abcd1234", false, false);
        } catch (UserException ex) {
            assertEquals("Username must be different from Password", ex.getMessage());
            return;
        }

        fail("should fail if password matches username");
    }

    private void assertPasswordInvalidOnContentRulesFailure(String password) {
        User user = new User();
        try {
            user.setPassword(password);
        } catch (UserException ex) {
            assertEquals("One or more characters of password must be a non-letter", ex.getMessage());
            return;
        }

        fail("should fail when password does not contain atleast one non-alphabetic character");
    }

    private void assertInvalidPasswordDueToSize(String password) {
        User user = new User();
        try {
            user.setPassword(password);
        } catch (UserException ex) {
            assertEquals("Password must have at least 8 characters", ex.getMessage());
            return;
        }

        fail("should fail when password is less than 8 characters in lengh");
    }

    public void testShouldFailIfAffiliationHasLessThanThreeCharacters() {
        assertInvalidAffiliatioDueToSize("a");
        assertInvalidAffiliatioDueToSize("1");
        assertInvalidAffiliatioDueToSize("ab"); 
    }

    public void testShouldFailIfAffiliationHasLessThanThreeCharacatersOnConstruction() {
        try {
            new User(null, "ab", null, null, "abcd", "abcd1234", false, false);
        } catch (UserException ex) {
            assertEquals("Affiliation must have 2 or more characters", ex.getMessage());
            return;
        }

        fail("should fail when affiliation is less than 3 characters in lengh");
    }

    public void testShouldPassIfAffiliationHasThreeOrMoreCharacters() throws UserException {
        new User(null, "abc", null, null, "abcd", "abcd1234", false, false);
        new User(null, "abc34", null, null, "abcd", "abcd1234", false, false);
    }
    
    private void assertInvalidAffiliatioDueToSize(String affiliation) {
        User user = new User();
        
        try {
            user.setAffiliation(affiliation);
        } catch (UserException ex) {
            assertEquals("Affiliation must have 2 or more characters", ex.getMessage());
            return;
        }

        fail("should fail when affiliation is less than 3 characters in lengh");
    }
}
