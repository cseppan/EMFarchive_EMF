package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public abstract class UserServiceTestCase extends ServicesTestCase {
    private UserService service;

    private HibernateSessionFactory sessionFactory;

    private Session session;

    public void setUpService(UserService service) throws Exception {
        this.service = service;

        sessionFactory = new HibernateSessionFactory(sessionFactory());
        session = sessionFactory.getSession();
    }

    protected void doTearDown() throws Exception {
        session.close();
    }

    public void testGetUserSucceedsForEMFAdministrator() throws Exception {
        User user = service.getUser("admin");
        assertEquals("EMF Administrator", user.getFullName());
    }

    public void testCreateUser() throws Exception {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("user12345");
        user.setFullName("test");
        user.setAffiliation("aff");
        user.setPhone("111-222-3333");
        user.setEmail("email@email.edu");

        int initialCount = service.getUsers().length;

        service.createUser(user);

        try {
            assertNotNull(service.getUser("test-user"));
            assertEquals(initialCount + 1, service.getUsers().length);
        } finally {
            remove(user);
        }
    }

    public void testShouldAuthenticateSuccessfullyIfUsernamePasswordMatchExpected() throws Exception {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("user12345");
        user.setFullName("test");
        user.setAffiliation("aff");
        user.setPhone("111-222-3333");
        user.setEmail("email@email.edu");

        service.createUser(user);

        try {
            service.authenticate(user.getUsername(), user.getEncryptedPassword());
        } finally {
            remove(user);
        }
    }

    public void testShouldFailAuthenticateIfUsernamePasswordDoNotMatchExpected() throws Exception {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("user12345");
        user.setFullName("test");
        user.setAffiliation("aff");
        user.setPhone("111-222-3333");
        user.setEmail("email@email.edu");

        service.createUser(user);

        try {
            service.authenticate(user.getUsername(), "invalid passwd");
        } catch (EmfException ex) {
            return;
        } finally {
            remove(user);
        }

        fail("should have failed authentication due to invalid password");
    }

    public void testShouldFailAuthenticateIfUserAccountIsDisabled() throws Exception {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("user12345");
        user.setFullName("test");
        user.setAffiliation("aff");
        user.setPhone("111-222-3333");
        user.setEmail("email@email.edu");
        user.setAcctDisabled(true);

        service.createUser(user);

        try {
            service.authenticate(user.getUsername(), user.getEncryptedPassword());
        } catch (EmfException ex) {
            return;
        } finally {
            remove(user);
        }

        fail("should have failed authentication due to disabled account");
    }

    public void testShouldFailAuthenticateIfUserDoesNotExist() throws Exception {
        try {
            service.authenticate("random user", "invalid passwd");
        } catch (EmfException ex) {
            return;
        }

        fail("should have failed authentication due to invalid user");
    }

    private void remove(User user) {
        Transaction tx = session.beginTransaction();
        session.delete(user);
        tx.commit();
    }

    public void testUpdateUser() throws Exception {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("user12345");
        user.setFullName("name");
        user.setAffiliation("aff");
        user.setPhone("111-222-3333");
        user.setEmail("email@email.edu");

        service.createUser(user);

        user.setFullName("modified-name");
        service.updateUser(user);

        try {
            User result = service.getUser("test-user");
            assertNotNull(result);
            assertEquals("modified-name", result.getFullName());
        } finally {
            remove(user);
        }
    }

    public void testDeleteUser() throws Exception {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("user12345");
        user.setFullName("name");
        user.setAffiliation("aff");
        user.setPhone("111-222-3333");
        user.setEmail("email@email.edu");

        service.createUser(user);

        // test
        service.deleteUser(user);

        User result = getUser(user.getUsername());
        assertNull("User should have been deleted", result);
    }

    private User getUser(String username) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(User.class).add(Restrictions.eq("username", username));
            tx.commit();

            return (User) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
