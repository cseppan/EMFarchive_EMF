package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.UserException;

public interface PopulateUserStrategy {

    void populate(String name, String affiliation, String phone, String email, String username, char[] password,
            char[] confirmPassword) throws UserException;

}
