package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.HibernateTestCase;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;

public class UsersDaoTest extends HibernateTestCase {

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldFetchAllUsers() {
        UsersDao dao = new UsersDao();

        List users = dao.getAll(session);
        assertTrue("Should contain at least 2 users", users.size() >= 2);
    }

    public void testShouldAddUser() throws Exception {
        UsersDao dao = new UsersDao();

        List usersBeforeAdd = dao.getAll(session);

        User user = new User();
        user.setUsername("user-dao-test");
        user.setPassword("abc12345");
        user.setFullName("user dao");
        user.setAffiliation("test");
        user.setPhone("123-123-1234");
        user.setEmail("email@user-test.test");

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

    public void testShouldRemoveUser() throws Exception {
        UsersDao dao = new UsersDao();

        List usersBeforeAdd = dao.getAll(session);

        User user = new User();
        user.setUsername("user-dao-test");
        user.setPassword("abc12345");
        user.setFullName("user dao");
        user.setAffiliation("test");
        user.setPhone("123-123-1234");
        user.setEmail("email@user-test.test");

        dao.add(user, session);

        // test
        dao.remove(user, session);

        // assert
        List usersAfterRemove = dao.getAll(session);
        assertEquals(usersBeforeAdd.size(), usersAfterRemove.size());
    }

    public void testShouldUpdateUser() throws Exception {
        UsersDao dao = new UsersDao();

        User user = new User();
        user.setUsername("user-dao-test");
        user.setPassword("abc12345");
        user.setFullName("user dao");
        user.setAffiliation("test");
        user.setPhone("123-123-1234");
        user.setEmail("email@user-test.test");

        dao.add(user, session);

        // test
        User added = user(user.getUsername());
        added.setFullName("updated name");

        dao.update(added, session);

        // assert
        try {
            User updated = user(user.getUsername());
            assertEquals("updated name", updated.getFullName());
        } finally {
            remove(added);
        }
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
