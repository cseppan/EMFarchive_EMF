package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.services.User;

public class NoOpPostRegisterStrategy implements PostRegisterStrategy {

    public void execute(User user) {
    }

}
