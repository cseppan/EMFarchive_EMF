package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.AuthenticationException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.UsersDao;
import gov.epa.emissions.framework.services.UserService;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class UserServiceImpl implements UserService {

    private static Log LOG = LogFactory.getLog(UserServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private UsersDao dao;

    public UserServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public UserServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new UsersDao();
    }

    public void authenticate(String username, String password) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            User user = dao.get(username, session);
            if (user == null)
                throw new AuthenticationException("Invalid username");

            if (user.isAcctDisabled())
                throw new AuthenticationException("Account Disabled");

            session.close();

            if (!user.getEncryptedPassword().equals(password))
                throw new AuthenticationException("Incorrect Password");
        } catch (HibernateException ex) {
            LOG.error(ex);
            throw new EmfException("Could not authenticate user: " + username + ". Reason: " + ex.getMessage());
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
            List all = dao.getAll(session);
            session.close();

            return (User[]) all.toArray(new User[0]);
        } catch (HibernateException e) {
            LOG.error("Could not get all Users. Reason: " + e);
            throw new EmfException("Could not get all Users.");
        }
    }

    public void createUser(User user) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.add(user, session);
            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not create new user - " + user.getFullName() + ". Reason: " + e.getMessage());
            throw new EmfException("Could not create new user - " + user.getFullName());
        }
    }

    public void updateUser(User user) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.update(user, session);
            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not update user - " + user.getFullName() + ". Reason: " + e.getMessage());
            throw new EmfException("Could not update user - " + user.getFullName());
        }
    }

    public void deleteUser(User user) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.remove(user, session);
            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not delete user - " + user.getFullName() + ". Reason: " + e.getMessage());
            throw new EmfException("Could not delete user - " + user.getFullName());
        }
    }

}
