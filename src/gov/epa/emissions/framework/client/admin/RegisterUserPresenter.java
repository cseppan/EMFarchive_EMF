package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

public class RegisterUserPresenter {

    private EMFUserAdmin model;
    private RegisterUserView view;

    public RegisterUserPresenter(EMFUserAdmin model, RegisterUserView view) {
        this.model = model;
        this.view = view;
    }

    public void notifyCreate() throws EmfException {
        User user = new User();
        
        user.setUserName(view.getUsername());
        user.setPassword(view.getPassword());
        user.confirmPassword(view.getConfirmPassword());
        
        user.setFullName(view.getFullName());
        user.setEmailAddr(view.getEmail());
        user.setWorkPhone(view.getPhone());
        user.setAffiliation(view.getAffiliation());
        
        model.createUser(user);
        model.authenticate(user.getUserName(), user.getPassword(), false);//TODO: admin status ?        
    }

    public void notifyCancel() {
        view.close();
    }

    public void init() {
        view.setObserver(this);
    }

}
