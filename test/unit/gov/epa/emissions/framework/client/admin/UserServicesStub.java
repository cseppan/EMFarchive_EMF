package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.util.Iterator;
import java.util.List;

public class UserServicesStub implements UserServices {

    private List users;

    public UserServicesStub(List users) {
        this.users = users;
    }

    public void authenticate(String userName, String pwd, boolean wantAdminStatus) {
        // TODO
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
        // TODO
    }

    public void deleteUser(String userName) {
        for (Iterator iter = users.iterator(); iter.hasNext();) {
            User user = (User) iter.next();
            if (user.getUserName().equals(userName)) {
                users.remove(user);
                break;
            }

        }
    }

    public void updateUsers(User[] users) {
        // TODO Auto-generated method stub
    }

}
