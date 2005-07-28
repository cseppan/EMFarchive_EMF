package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfPresenter;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

public class RegisterUserPresenter implements EmfPresenter {

    private EMFUserAdmin model;
    private RegisterUserView view;

    public RegisterUserPresenter(EMFUserAdmin model, RegisterUserView view) {
        this.model = model;
        this.view = view;
        this.view.setObserver(this);
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
        
        //TODO: should not autologin if request is from UserManager
        model.authenticate(user.getUserName(), user.getPassword(), false);//TODO: admin status ?        
    }

    public void notifyCancel() {
        view.close();
    }

}
