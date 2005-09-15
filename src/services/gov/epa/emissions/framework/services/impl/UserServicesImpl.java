/*
 * Created on Jun 21, 2005
 *
 */
package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.AuthenticationException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.dao.UserManagerDAO;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author adm$wkstn
 * 
 */
public class UserServicesImpl implements UserServices {
    private static Log log = LogFactory.getLog(UserServicesImpl.class);

    /**
     * 
     */
    public UserServicesImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#authenticate(java.lang.String,
     *      java.lang.String, boolean)
     */
    public void authenticate(String userName, String pwd) throws EmfException {

        log.debug("Called authenticate for username= " + userName);

        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            User emfUser = umDAO.getUser(userName);
            if (emfUser == null) {
                log.error("In the manager.  emfUser was NULL");
                throw new AuthenticationException("Invalid username");
            }// emfUser is null

            if (emfUser.isAcctDisabled()) {
                log.error("User data error: account disabled: " + userName);
                throw new AuthenticationException("Account Disabled");
            }
            if (!emfUser.getEncryptedPassword().equals(pwd)) {
                log.debug("User data error: incorrect password " + userName);
                log.debug("Pwd in User: " + emfUser.getEncryptedPassword());
                log.debug("pwd from login: " + pwd);
                throw new AuthenticationException("Incorrect Password");
            }
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }

        log.debug("Called authenticate for username= " + userName);

    }// authenticate

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#resetPassword()
     */
    public boolean resetPassword() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#getUser(java.lang.String)
     */
    public User getUser(String userName) throws EmfException {
        log.debug("In get user " + userName);
        UserManagerDAO umDAO;
        User user = null;
        try {
            umDAO = new UserManagerDAO();
            user = umDAO.getUser(userName);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
        log.debug("In get user " + userName);
        return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#createUser(gov.epa.emissions.framework.commons.User)
     */
    public void createUser(User newUser) throws EmfException {
        log.debug("In create new user: " + newUser.getUsername());
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            if (umDAO.isNewUser(newUser.getUsername())) {
                umDAO.insertUser(newUser);
            } else {
                log.error("User data error: Duplicate username: " + newUser.getUsername());
                throw new UserException("Duplicate username");
            }
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#updateUser(gov.epa.emissions.framework.commons.User)
     */
    public void updateUser(User newUser) throws EmfException {
        log.debug("updating user info: " + newUser.getUsername());
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            umDAO.updateUser(newUser);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
        log.debug("updating user info: " + newUser.getUsername());
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#deleteUser(java.lang.String)
     */
    public void deleteUser(String userName) throws EmfException {
        log.debug("Delete user " + userName);
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            umDAO.deleteUser(userName);
        } catch (InfrastructureException ex) {
            ex.printStackTrace();
            throw new EmfException(ex.getMessage());
        }
        log.debug("Delete user " + userName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#getUsers()
     */

    public User[] getUsers() throws EmfException {
        log.debug("get all users");
        User[] users = null;

        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            List allUsers = umDAO.getUsers();
            users = (User[]) allUsers.toArray(new User[allUsers.size()]);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
        log.debug("get all users");
        return users;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.commons.EMFUserAdmin#updateUsers(gov.epa.emissions.framework.commons.User[])
     */
    public void updateUsers(User[] users) throws EmfException {
        log.debug("Start update Users");
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            umDAO.updateUsers(users);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
        log.debug("End Update Users");

    }

}
