package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.AuthenticationException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.UserDAO;
import gov.epa.emissions.framework.services.UserService;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class UserServiceImpl implements UserService {
    private static Log LOG = LogFactory.getLog(UserServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private UserDAO dao;

    public UserServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public UserServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new UserDAO();
    }

    public void authenticate(String username, String password) throws EmfException {
        try {
            User user = getUser(username);
            if (user == null)
                throw new AuthenticationException("User " + username + " does not exist");

            if (user.isAccountDisabled())
                throw new AuthenticationException("Account Disabled");

            if (!user.getEncryptedPassword().equals(password))
                throw new AuthenticationException("Incorrect Password");
        } catch (RuntimeException e){
            LOG.error("Unable to authenticate due to data access failure" + username, e);
            throw new EmfException("Unable to authenticate due to data access failure");
        }
    }

    public User getUser(String username) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            User user = dao.get(username, session);
            session.close();

            return user;
        } catch (RuntimeException e) {
            LOG.error("Could not get User - " + username, e);
            throw new EmfException("Could not get User due to data access failure");
        }
    }

    public User[] getUsers() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List all = dao.all(session);
            session.close();

            return (User[]) all.toArray(new User[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Users", e);
            throw new EmfException("Unable to fetch all users due to data access failure");
        }
    }

    public void createUser(User user) throws EmfException {
        User existingUser = this.getUser(user.getUsername());
        if (existingUser != null) {
            throw new EmfException("Could not create new user. The username '" + user.getUsername()
                    + "' is already taken");
        }

        try {
            Session session = sessionFactory.getSession();
            dao.add(user, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not create new user - " + user.getUsername(),e);
            throw new EmfException("Unable to fetch user due to data access failure");
        }
    }

    public void updateUser(User user) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.update(user, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not update user - " + user.getName(),e);
            throw new EmfException("Unable to update user due to data access failure");
        }
    }

    public void deleteUser(User user) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.remove(user, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not delete user - " + user.getName(),e);
            throw new EmfException("Unable to delete user due to data access failure");
        }
    }

    public User obtainLocked(User owner, User object) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            User locked = dao.obtainLocked(owner, object, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for user: " + object.getUsername() + " by owner: " + owner.getUsername(),e);
            throw new EmfException("Unable to fetch lock user due to data access failure");
        }
    }

    public User releaseLocked(User object) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            User released = dao.releaseLocked(object, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for user: " + object.getUsername() + " by owner: "
                    + object.getLockOwner(),e);
            throw new EmfException("Unable to release lock on user due to data access failure");
        }
    }

}
