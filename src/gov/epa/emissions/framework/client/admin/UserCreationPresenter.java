package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

public class UserCreationPresenter {

    private EMFUserAdmin model;
    private UserCreationView view;

    public UserCreationPresenter(EMFUserAdmin model, UserCreationView view) {
        this.model = model;
        this.view = view;
    }

    public void notifyCreate() throws EmfException {
        User user = new User();
        user.setUserName(view.getUsername());
        user.setPassword(view.getPassword());
        
        user.setFullName(view.getName());
        user.setEmailAddr(view.getEmail());
        user.setWorkPhone(view.getPhone());
        user.setAffiliation(view.getAffiliation());
        
        model.createUser(user);
    }

    public void notifyCancel() {
        view.close();
    }

}
