package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.commons.User;

public class UsersManager {

    private EMFUserAdmin usersAdmin;

    public UsersManager(EMFUserAdmin usersAdmin) {
        this.usersAdmin = usersAdmin;
    }

    public void createUser(User user) {
        this.usersAdmin.createUser(user);
    }

}
