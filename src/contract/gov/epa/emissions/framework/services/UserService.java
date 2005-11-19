package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

/**
 * Provides services for Login and Administrative functions
 */
public interface UserService extends EMFService {

    public void authenticate(String username, String password) throws EmfException;

    public User getUser(String userName) throws EmfException;

    public User[] getUsers() throws EmfException;

    public void createUser(User newUser) throws EmfException;

    public void updateUser(User newUser) throws EmfException;

    // FIXME: should use a User object instead
    public void deleteUser(String userName) throws EmfException;

}
