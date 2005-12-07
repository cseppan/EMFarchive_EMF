package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.UserService;

import java.util.Iterator;
import java.util.List;

public class UserServiceStub implements UserService {

    private List users;

    public UserServiceStub(List users) {
        this.users = users;
    }

    public void authenticate(String userName, String pwd) {
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
            if (user.getUsername().equals(userName)) {
                users.remove(user);
                break;
            }

        }
    }

}
