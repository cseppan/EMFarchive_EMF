package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.services.User;

public interface PostRegisterStrategy {
    void execute(User user);
}
