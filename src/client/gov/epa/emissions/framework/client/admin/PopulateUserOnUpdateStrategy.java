package gov.epa.emissions.framework.client.admin;

import java.util.Date;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.security.UserException;
import gov.epa.emissions.framework.services.EmfException;

public class PopulateUserOnUpdateStrategy implements PopulateUserStrategy {

    private User user;

    public PopulateUserOnUpdateStrategy(User user) {
        this.user = user;
    }

    public void populate(String name, String affiliation, String phone, String email, String username, char[] password,
            char[] confirmPassword, Boolean wantEmails) throws EmfException {
        try {
            user.setName(name);
            user.setAffiliation(affiliation);
            user.setPhone(phone);
            user.setEmail(email);
            if (password.length > 0) {
                user.setPassword(new String(password));
                user.confirmPassword(new String(confirmPassword));
                user.setPasswordResetDate(new Date());
            }
            user.setWantEmails(wantEmails);
        } catch (UserException e) {
            throw new EmfException(e.getMessage());
        }
    }

}
