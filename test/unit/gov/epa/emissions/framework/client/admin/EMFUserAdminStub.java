package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUser;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.util.Iterator;
import java.util.List;

public class EMFUserAdminStub implements EMFUserAdmin {

    private List users;

    public EMFUserAdminStub(List users) {
        this.users = users;
    }

    public boolean isNewUser() {
        return false;
    }

    public boolean validate() {
        return false;
    }

    public String authenticate(String userName, String pwd, boolean wantAdminStatus) {
        return null;
    }

    public boolean resetPassword() {
        return false;
    }

    public User getUser(String userName) {
        return null;
    }

    public User[] getUsers() {
        return (User[]) users.toArray(new User[0]);
    }

    public String createUser(User newUser) {
        return null;
    }

    public String updateUser(User newUser) {
        return null;
    }

    public String updateUsers(List users) {
        return null;
    }

    public String deleteUser(String userName) {
        for (Iterator iter = users.iterator(); iter.hasNext();) {
            User user = (User) iter.next();
            if(user.getUserName().equals(userName)) {
                users.remove(user);
                break;
            }
            
        }
        return null;
    }

    public EMFUser[] getEmfUsers() throws EmfException {
        return null;
    }

}
