package gov.epa.emissions.framework.commons;

import gov.epa.emissions.framework.UserException;
import junit.framework.TestCase;

public class UserTest extends TestCase {

    public void testShouldFailIfNameIsNull() {
        User user = new User();
        try {
            user.setFullName(null);
        } catch (UserException ex) {
            assertEquals("Name should be specified", ex.getMessage());
            return;
        }

        fail("should fail if Name is unspecified");
    }

    public void testShouldFailIfUsernameIsLessThanThreeCharacters() throws UserException {
        assertInvalidUsername("a");
        assertInvalidUsername("ab");
        assertInvalidUsername("");
        try {
            new User("sdfsdfsdf", "abc", "123", "a@a.org", "a2", null, false, false);
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
            user.confirmPassword("password12345");
        } catch (UserException ex) {
            assertEquals("Confirm Password 'password12345' does not match Password 'password123'", ex.getMessage());
            return;
        }

        fail("should fail if Password does not match Confirm Password");
    }

    public void testShouldAllowUsernameIfSizeIsGreaterThan2CharactersOnConstruction() throws UserException {
        new User("sdfsf", "abc", "123", "a@a.org", "ab62", "abcd1234", false, false);
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
            new User("sdfsdf", "abc", "123", "a@a.org", "abc", "1234567", false, false);
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
            new User("SdfsdfSD", "abd", "123", "a@a.org", "abcd1234", "abcd1234", false, false);
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

    public void testShouldFailIfPasswordIsUnspecified() {
        User user = new User();
        try {
            user.setPassword(null);
        } catch (UserException ex) {
            assertEquals("Password should be specified", ex.getMessage());
            return;
        }

        fail("should fail when Password is unspecified");
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
            new User("Sdfsf", "ab", null, null, "abcd", "abcd1234", false, false);
        } catch (UserException ex) {
            assertEquals("Affiliation should have 2 or more characters", ex.getMessage());
            return;
        }

        fail("should fail when affiliation is less than 3 characters in lengh");
    }

    public void testShouldPassIfAffiliationHasThreeOrMoreCharacters() throws UserException {
        new User("Sdfs", "abc", "123", "a@a.org", "abcd", "abcd1234", false, false);
        new User("werw", "abc34", "123", "a@a.org", "abcd", "abcd1234", false, false);
    }

    public void testShouldFailIfAffiliationIsUnspecified() {
        User user = new User();

        try {
            user.setAffiliation(null);
        } catch (UserException ex) {
            assertEquals("Affiliation should hbe specified", ex.getMessage());
            return;
        }

        fail("should fail when Affiliation is unspecified");
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
            new User("sdf", "abc", "12d", null, "abcd", "abcd1234", false, false);
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
            new User("fghas", "abc", "12", "ab@", "abcd", "abcd1234", false, false);
        } catch (UserException ex) {
            assertEquals("Email should have the format xx@yy.zz", ex.getMessage());
            return;
        }

        fail("should fail when Email is in invalid format");
    }

    private void assertInvalidEmail(String email) {
        User user = new User();

        try {
            user.setEmailAddr(email);
        } catch (UserException ex) {
            assertEquals("Email should have the format xx@yy.zz", ex.getMessage());
            return;
        }

        fail("should fail when Email is in invalid format");
    }

    public void testShouldFailWhenEmailIsUnspecified() {
        User user = new User();

        try {
            user.setEmailAddr(null);
        } catch (UserException ex) {
            assertEquals("Email should be specified", ex.getMessage());
            return;
        }

        fail("should fail when Email is unspecified");
    }

}
