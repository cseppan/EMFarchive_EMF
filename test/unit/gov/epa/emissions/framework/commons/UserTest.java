package gov.epa.emissions.framework.commons;

import gov.epa.emissions.framework.UserException;
import junit.framework.TestCase;

public class UserTest extends TestCase {

    public void testShouldFailIfUsernameIsLessThanThreeCharacters() throws UserException {
        assertInvalidUsername("a");
        assertInvalidUsername("ab");
        assertInvalidUsername("");
        try {
            new User(null, "abc", "123", "a@a.org", "a2", null, false, false);            
        } catch (UserException ex) {
            assertEquals("Username should have at least 3 characters", ex.getMessage());
            return;
        }

        fail("should fail if username is less than 3 characters");
    }

    public void testShouldFailIfPasswordDoesNotMatchConfirmPassword() {
        User user = new User();
        try {
            user.setPassword("password123");
            user.confirmPassword("psdfssdfsdf21");
        } catch (UserException ex) {
            assertEquals("Password does not match Confirm Password", ex.getMessage());
            return;
        }

        fail("should fail if Password does not match Confirm Password");       
    }
    
    public void testShouldAllowUsernameIfSizeIsGreaterThan2CharactersOnConstruction() throws UserException {
        new User(null, "abc", "123", "a@a.org", "ab62", "abcd1234", false, false);
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
            assertEquals("Username should have at least 3 characters", ex.getMessage());
            return;
        }

        fail("should fail if username is less than 3 characters");
    }

    public void testShouldFailIfPasswordSizeIsLessThanEightCharacters() throws UserException {
        assertInvalidPasswordDueToSize("");
        assertInvalidPasswordDueToSize("a");
        assertInvalidPasswordDueToSize("1234567");
    }

    public void testShouldFailIfPasswordSizeIsLessThanEightCharactersOnConstruction() {
        try {
            new User(null, "abc", "123", "a@a.org", "abc", "1234567", false, false);
        } catch (UserException ex) {
            assertEquals("Password should have at least 8 characters", ex.getMessage());
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
            assertEquals("Username should be different from Password", ex.getMessage());
            return;
        }

        fail("should fail if password matches username");
    }
    
    public void testShouldFailIfUsernameIsUnspecified() throws UserException {
        User user = new User();

        try {
            user.setUserName(null);
        } catch (UserException ex) {
            assertEquals("Username should be specified", ex.getMessage());
            return;
        }

        fail("should fail if username is unspecified");
    }

    public void testShouldFailIfUsernameMatchesPasswordOnSetUsername() throws UserException {
        User user = new User();
        user.setPassword("abcdefg1");

        try {
            user.setUserName("abcdefg1");
        } catch (UserException ex) {
            assertEquals("Username should be different from Password", ex.getMessage());
            return;
        }

        fail("should fail if password matches username");
    }

    public void testShouldFailIfUsernameMatchesPasswordOnConstruction() throws UserException {
        try {
            new User(null, "abd", "123", "a@a.org", "abcd1234", "abcd1234", false, false);
        } catch (UserException ex) {
            assertEquals("Username should be different from Password", ex.getMessage());
            return;
        }

        fail("should fail if password matches username");
    }

    private void assertPasswordInvalidOnContentRulesFailure(String password) {
        User user = new User();
        try {
            user.setPassword(password);
        } catch (UserException ex) {
            assertEquals("One or more characters of password should be a non-letter", ex.getMessage());
            return;
        }

        fail("should fail when password does not contain atleast one non-alphabetic character");
    }

    private void assertInvalidPasswordDueToSize(String password) {
        User user = new User();
        try {
            user.setPassword(password);
        } catch (UserException ex) {
            assertEquals("Password should have at least 8 characters", ex.getMessage());
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
            assertEquals("Affiliation should have 2 or more characters", ex.getMessage());
            return;
        }

        fail("should fail when affiliation is less than 3 characters in lengh");
    }

    public void testShouldPassIfAffiliationHasThreeOrMoreCharacters() throws UserException {
        new User(null, "abc", "123", "a@a.org", "abcd", "abcd1234", false, false);
        new User(null, "abc34", "123", "a@a.org", "abcd", "abcd1234", false, false);
    }

    private void assertInvalidAffiliatioDueToSize(String affiliation) {
        User user = new User();

        try {
            user.setAffiliation(affiliation);
        } catch (UserException ex) {
            assertEquals("Affiliation should have 2 or more characters", ex.getMessage());
            return;
        }

        fail("should fail when affiliation is less than 3 characters in lengh");
    }

    public void testShouldFailIfPhoneIsInInvalidFormat() {
        assertInvalidPhoneFormat("1x");
        assertInvalidPhoneFormat("ab");
    }

    public void testShouldFailIfPhoneIsUnspecified() {
        User user = new User();

        try {
            user.setWorkPhone(null);
        } catch (UserException ex) {
            assertEquals("Phone should be specified", ex.getMessage());
            return;
        }

        fail("should fail when Phone is unspecified");
    }

    public void testShouldFailIfPhoneIsInvalidOnConstruction() {
        try {
            new User(null, "abc", "12d", null, "abcd", "abcd1234", false, false);
        } catch (UserException ex) {
            assertEquals("Phone should have format xxx-yyy-zzzz or xxxx or x-yyyy", ex.getMessage());
            return;
        }

        fail("should fail when Phone has invalid format");
    }

    private void assertInvalidPhoneFormat(String phone) {
        User user = new User();

        try {
            user.setWorkPhone(phone);
        } catch (UserException ex) {
            assertEquals("Phone should have format xxx-yyy-zzzz or xxxx or x-yyyy", ex.getMessage());
            return;
        }

        fail("should fail when Phone has invalid format");
    }

    public void testShouldFailIfEmailHasInvalidFormat() {
        assertInvalidEmail("a");
        assertInvalidEmail("1");
        assertInvalidEmail("ab");
        assertInvalidEmail("ab@");
        assertInvalidEmail("ab@.");
        assertInvalidEmail("ab2");
        assertInvalidEmail("ab@s.");
        assertInvalidEmail("ab@.s.23");
        assertInvalidEmail("ab@sd2..");
        assertInvalidEmail("@s.sdr");
    }

    public void testShouldFailOnConstructionIfEmailIsInvalid() {
        try {
            new User(null, "abc", "12", "ab@", "abcd", "abcd1234", false, false);
        } catch (UserException ex) {
            assertEquals("Email should have 2 or more characters", ex.getMessage());
            return;
        }

        fail("should fail when Phone is less than 3 characters in lengh");
    }

    private void assertInvalidEmail(String email) {
        User user = new User();

        try {
            user.setEmailAddr(email);
        } catch (UserException ex) {
            assertEquals("Email should have 2 or more characters", ex.getMessage());
            return;
        }

        fail("should fail when Phone is less than 3 characters in lengh");
    }

}
