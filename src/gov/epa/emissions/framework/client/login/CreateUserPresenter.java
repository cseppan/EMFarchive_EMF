package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

public class CreateUserPresenter {

    private EMFUserAdmin userAdmin;

    public CreateUserPresenter(EMFUserAdmin userAdmin) {
        this.userAdmin = userAdmin;
    }

    public void create(String name, String affiliation, String phone, String email, String username, String password,
            String confirmPassword) throws EmfException {
        // TODO: what's the two extra params @ the end ?
        User user = new User(name, affiliation, phone, email, username, password, false, false);

        userAdmin.createUser(user);
        
        userAdmin.authenticate(user.getUserName(), user.getPassword(), false);
    }

}
