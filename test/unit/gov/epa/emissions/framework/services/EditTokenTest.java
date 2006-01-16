package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;
import junit.framework.TestCase;

public class EditTokenTest extends TestCase {

    public void testTokenShouldHaveUser() {
        EditToken token = new EditToken();
        User user = new User();

        token.setUser(user);

        assertSame(user, token.getUser());
    }
}
