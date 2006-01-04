package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.AuthenticationException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.dao.UserManagerDAO;
import gov.epa.emissions.framework.services.UserService;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserServiceImpl extends EmfServiceImpl implements UserService {

    private UserManagerDAO dao;

    public UserServiceImpl(DataSource datasource, DbServer dbServer) throws EmfException {
        super(datasource, dbServer);
        init(datasource);
    }

    public UserServiceImpl() throws Exception {
        init(datasource);
    }

    private void init(DataSource datasource) throws EmfException {
        try {
            dao = new UserManagerDAO(datasource);
        } catch (SQLException e) {
            throw new EmfException("Could not initialize User Service. Reason: " + e.getMessage());
        }
    }

    private static Log log = LogFactory.getLog(UserServiceImpl.class);

    public void authenticate(String userName, String pwd) throws EmfException {
        try {
            User emfUser = dao.get(userName);
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
            return dao.get(userName);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
    }

    public void createUser(User newUser) throws EmfException {
        try {
            if (!dao.contains(newUser.getUsername())) {
                log.error("User data error: Duplicate username: " + newUser.getUsername());
                throw new EmfException("Duplicate username");
            }

            dao.add(newUser);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
    }

    public void updateUser(User newUser) throws EmfException {
        try {
            dao.update(newUser);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
    }

    public void deleteUser(String userName) throws EmfException {
        try {
            dao.remove(userName);
        } catch (InfrastructureException ex) {
            throw new EmfException(ex.getMessage());
        }
    }

    public User[] getUsers() throws EmfException {
        try {
            List allUsers = dao.all();
            return (User[]) allUsers.toArray(new User[allUsers.size()]);
        } catch (InfrastructureException ex) {
            log.error(ex);
            throw new EmfException(ex.getMessage());
        }
    }

}
