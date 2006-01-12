package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;

/**
 * Provides services for Login and Administrative functions
 */
public interface UserService extends EMFService {

    void authenticate(String username, String password) throws EmfException;

    User getUser(String username) throws EmfException;

    User[] getUsers() throws EmfException;

    void createUser(User user) throws EmfException;

    /**
     * should obtain lock before updating. On completion of update, the lock is released implicitly.
     */
    void updateUser(User user) throws EmfException;

    void deleteUser(User user) throws EmfException;

    User obtainLocked(User owner, User object) throws EmfException;

    User releaseLocked(User object) throws EmfException;

}
