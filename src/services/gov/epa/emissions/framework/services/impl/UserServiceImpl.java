package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.AuthenticationException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.dao.UserManagerDAO;
import gov.epa.emissions.framework.services.UserService;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserServiceImpl extends EmfServiceImpl implements UserService {

    private UserManagerDAO dao;

    public UserServiceImpl(DataSource datasource, DbServer dbServer) {
        super(datasource, dbServer);
        init(datasource);
    }

    public UserServiceImpl() throws Exception {
        init(datasource);
    }

    private void init(DataSource datasource) {
        dao = new UserManagerDAO(datasource);
    }

    private static Log log = LogFactory.getLog(UserServiceImpl.class);

    public void authenticate(String userName, String pwd) throws EmfException {
        try {
            User emfUser = dao.getUser(userName);
            if (emfUser == null) {
                log.error("In the manager.  emfUser was NULL");
                throw new AuthenticationException("Invalid username");
            }

            if (emfUser.isAcctDisabled()) {
                log.error("User data error: account disabled: " + userName);
                throw new AuthenticationException("Account Disabled");
            }

            if (!emfUser.getEncryptedPassword().equals(pwd))
                throw new AuthenticationException("Incorrect Password");
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }

        log.debug("Called authenticate for username= " + userName);

    }

    public User getUser(String userName) throws EmfException {
        try {
            return dao.getUser(userName);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
    }

    public void createUser(User newUser) throws EmfException {
        try {
            if (!dao.isNewUser(newUser.getUsername())) {
                log.error("User data error: Duplicate username: " + newUser.getUsername());
                throw new EmfException("Duplicate username");
            }

            dao.insertUser(newUser);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
    }

    public void updateUser(User newUser) throws EmfException {
        try {
            dao.updateUser(newUser);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
    }

    public void deleteUser(String userName) throws EmfException {
        try {
            dao.deleteUser(userName);
        } catch (InfrastructureException ex) {
            throw new EmfException(ex.getMessage());
        }
    }

    public User[] getUsers() throws EmfException {
        try {
            List allUsers = dao.getUsers();
            return (User[]) allUsers.toArray(new User[allUsers.size()]);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
    }

}
