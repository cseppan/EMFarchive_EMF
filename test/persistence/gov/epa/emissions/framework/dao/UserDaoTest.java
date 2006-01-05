package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.HibernateTestCase;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;

public class UserDaoTest extends HibernateTestCase {

    private UserDao dao;

    private User user;

    protected void setUp() throws Exception {
        super.setUp();

        dao = new UserDao();
        user = newUser(dao);
    }

    protected void doTearDown() throws Exception {
        remove(user);
    }

    public void testShouldFetchAllUsers() {
        List users = dao.getAll(session);
        assertTrue("Should contain at least 2 users", users.size() >= 2);
    }

    public void testShouldAddUser() throws Exception {
        List usersBeforeAdd = dao.getAll(session);
        User user = newUser("add-user", dao);

        // test
        dao.add(user, session);

        // assert
        try {
            List usersAfterAdd = dao.getAll(session);
            assertEquals(usersBeforeAdd.size() + 1, usersAfterAdd.size());
        } finally {
            remove(user);
        }
    }

    public void testShouldGetSpecificUser() throws Exception {
        // test
        User loaded = dao.get(user.getUsername(), session);

        // assert
        assertEquals(user.getUsername(), loaded.getUsername());
        assertEquals(user.getFullName(), loaded.getFullName());
    }

    public void testShouldVerifyIfUserAlreadyExists() throws Exception {
        assertTrue("Should contain the added user", dao.contains(user.getUsername(), session));
    }

    public void testShouldRemoveUser() throws Exception {
        List usersBeforeRemove = dao.getAll(session);
        // test
        dao.remove(user, session);

        // assert
        try {
            List usersAfterRemove = dao.getAll(session);
            assertEquals(usersBeforeRemove.size(), usersAfterRemove.size() + 1);
        } finally {
            user = newUser(dao);// restore
        }
    }

    public void testShouldUpdateUser() throws Exception {
        // test
        User added = user(user.getUsername());
        added.setFullName("updated name");

        dao.update(added, session);

        // assert
        User updated = user(user.getUsername());
        assertEquals("updated name", updated.getFullName());
    }

    public void testShouldObtainLockedUser() {
        User lockOwner = dao.get("admin", session);

        User lockedUser = dao.obtainLocked(lockOwner, user, session);
        assertEquals(lockedUser.getFullName(), user.getFullName());

        User userLoadedFromDb = user(lockedUser.getUsername());
        assertEquals(userLoadedFromDb.getFullName(), user.getFullName());
    }

    public void testShouldUpdateLockedUser() throws Exception {
        User emf = dao.get("emf", session);

        User modified1 = dao.obtainLocked(emf, user, session);
        assertEquals(modified1.getLockOwner(), emf.getUsername());
        modified1.setFullName("TEST");

        User modified2 = dao.updateLocked(modified1, session);
        assertEquals("TEST", modified1.getFullName());
        assertEquals(modified2.getLockOwner(), null);
    }

    public void testShouldReleaseLockOnReleaseLockedUser() throws Exception {
        User emf = dao.get("emf", session);

        User locked = dao.obtainLocked(emf, user, session);
        User released = dao.releaseLocked(locked, session);
        assertFalse("Should have released lock", released.isLocked());

        User loadedFromDb = user(user.getUsername());
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    public void testShouldFailToReleaseSectorLockIfNotObtained() {
        try {
            dao.releaseLocked(user, session);
        } catch (EmfException e) {
            assertEquals("Cannot release without owning lock", e.getMessage());
            return;
        }

        fail("Should have failed to release lock that was not obtained");
    }

    private User newUser(UserDao dao) {
        return newUser("user-dao-test", dao);
    }

    private User newUser(String username, UserDao dao) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("abc12345");
        user.setFullName("user dao");
        user.setAffiliation("test");
        user.setPhone("123-123-1234");
        user.setEmail("email@user-test.test");

        dao.add(user, session);

        return user(user.getUsername());
    }

    private User user(String username) {
        List all = all();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            User element = (User) iter.next();
            if (element.getUsername().equals(username))
                return element;
        }

        return null;
    }

    private List all() {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List all = session.createCriteria(User.class).list();
            tx.commit();

            return all;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void remove(User user) {
        Transaction tx = session.beginTransaction();
        session.delete(user);
        tx.commit();
    }
}
