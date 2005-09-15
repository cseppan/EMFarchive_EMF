package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;

public class PopulateUserOnUpdateStrategy implements PopulateUserStrategy {

    private User user;

    public PopulateUserOnUpdateStrategy(User user) {
        this.user = user;
    }

    public void populate(String name, String affiliation, String phone, String email, String username, char[] password,
            char[] confirmPassword) throws UserException {
        user.setFullName(name);
        user.setAffiliation(affiliation);
        user.setPhone(phone);
        user.setEmail(email);
        if (password.length > 0) {
            user.setPassword(new String(password));
            user.confirmPassword(new String(confirmPassword));
        }
    }

}
