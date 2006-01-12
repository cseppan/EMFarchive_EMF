package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.UserDAO;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public abstract class UserServiceTestCase extends ServicesTestCase {
    private UserService service;

    public void setUpService(UserService service) throws Exception {
        this.service = service;
    }

    protected void doTearDown() throws Exception {// no op
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

        User loaded = user(user.getUsername());
        try {
            assertNotNull(loaded);
            assertEquals(initialCount + 1, service.getUsers().length);
        } finally {
            remove(loaded);
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

    public void testShouldObtainLockedUser() throws EmfException {
        UserDAO dao = new UserDAO();
        User user = newUser(dao);

        try {
            User owner = service.getUser("emf");

            User locked = service.obtainLocked(owner, user);
            assertTrue("Should be locked by owner", locked.isLocked(owner));
            assertEquals(user.getUsername(), locked.getUsername());

            // object returned directly from the table
            User loadedFromDb = user(locked.getUsername());
            assertEquals(locked.getLockOwner(), loadedFromDb.getLockOwner());
        } finally {
            remove(user);
        }
    }

    public void testShouldReleaseLockOnReleaseLockedUser() throws EmfException {
        UserDAO dao = new UserDAO();
        User target = newUser(dao);

        try {
            User owner = service.getUser("emf");

            User locked = service.obtainLocked(owner, target);
            User released = service.releaseLocked(locked);
            assertFalse("Should have released lock", released.isLocked());

            User loadedFromDb = user(locked.getUsername());
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(target);
        }
    }

    private User newUser(UserDAO dao) {
        User user = new User();
        user.setUsername("test-user");
        user.setPassword("abc12345");
        user.setFullName("user dao");
        user.setAffiliation("test");
        user.setPhone("123-123-1234");
        user.setEmail("email@user-test.test");

        dao.add(user, session);

        return user(user.getUsername());
    }

    private void remove(User user) {
        User loaded = user(user.getUsername());

        Transaction tx = session.beginTransaction();
        session.delete(loaded);
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

        User owner = service.getUser("emf");
        User locked = service.obtainLocked(owner, user);

        locked.setFullName("modified-name");
        service.updateUser(locked);

        try {
            User result = service.getUser("test-user");
            assertNotNull(result);
            assertEquals("modified-name", result.getFullName());
        } finally {
            remove(user);
        }
    }

    public void testDeleteUser() throws Exception {
        UserDAO dao = new UserDAO();
        User user = newUser(dao);

        User owner = service.getUser("emf");
        User locked = service.obtainLocked(owner, user);
        service.deleteUser(locked);

        User result = user(locked.getUsername());
        assertNull("User should have been deleted", result);
    }

    private User user(String username) {
        session.clear();

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
