package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

public class UsersManager {

    private EMFUserAdmin userAdmin;

    public UsersManager(EMFUserAdmin userAdmin) {
        this.userAdmin = userAdmin;
    }

    public void createUser(User user) throws EmfException {
        this.userAdmin.createUser(user);
    }

}
