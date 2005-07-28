package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
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

    public void authenticate(String userName, String pwd, boolean wantAdminStatus) {
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

    public void createUser(User newUser) {
        users.add(newUser);
    }

    public void updateUser(User newUser) {
    }

    public String updateUsers(List users) {
        return null;
    }

    public void deleteUser(String userName) {
        for (Iterator iter = users.iterator(); iter.hasNext();) {
            User user = (User) iter.next();
            if(user.getUserName().equals(userName)) {
                users.remove(user);
                break;
            }
            
        }
    }


    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFUserAdmin#updateUsers(gov.epa.emissions.framework.commons.User[])
     */
    public void updateUsers(User[] users) throws EmfException {
        // TODO Auto-generated method stub
        
    }

}
