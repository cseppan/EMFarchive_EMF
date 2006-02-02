package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.AuthenticationException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.UserDAO;
import gov.epa.emissions.framework.services.UserService;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
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
        } catch (HibernateException ex) {
            LOG.error("Could not authenticate user: " + username + ". Reason: " + ex.getMessage());
            throw new EmfException("Could not authenticate user: " + username);
        }
    }

    public User getUser(String username) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            User user = dao.get(username, session);
            session.close();

            return user;
        } catch (HibernateException e) {
            LOG.error("Could not get User - " + username + ". Reason: " + e);
            throw new EmfException("Could not get User - " + username);
        }
    }

    public User[] getUsers() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List all = dao.all(session);
            session.close();

            return (User[]) all.toArray(new User[0]);
        } catch (HibernateException e) {
            LOG.error("Could not get all Users. Reason: " + e);
            throw new EmfException("Could not get all Users.");
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
        } catch (HibernateException e) {
            LOG.error("Could not create new user - " + user.getUsername() + ". Reason: " + e.getMessage());
            throw new EmfException("Could not create new user - " + user.getUsername());
        }
    }

    public void updateUser(User user) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.update(user, session);
            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not update user - " + user.getName() + ". Reason: " + e.getMessage());
            throw new EmfException("Could not update user - " + user.getName());
        }
    }

    public void deleteUser(User user) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.remove(user, session);
            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not delete user - " + user.getName() + ". Reason: " + e.getMessage());
            throw new EmfException("Could not delete user - " + user.getName());
        }
    }

    public User obtainLocked(User owner, User object) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            User locked = dao.obtainLocked(owner, object, session);
            session.close();

            return locked;
        } catch (HibernateException e) {
            LOG.error("Could not obtain lock for user: " + object.getUsername() + " by owner: " + owner.getUsername()
                    + ".Reason: " + e);
            throw new EmfException("Could not obtain lock for user: " + object.getUsername() + " by owner: "
                    + owner.getUsername());
        }
    }

    public User releaseLocked(User object) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            User released = dao.releaseLocked(object, session);
            session.close();

            return released;
        } catch (HibernateException e) {
            LOG.error("Could not release lock for user: " + object.getUsername() + " by owner: "
                    + object.getLockOwner() + ".Reason: " + e);
            throw new EmfException("Could not obtain release for user: " + object.getUsername() + " by owner: "
                    + object.getLockOwner());
        }
    }

}
