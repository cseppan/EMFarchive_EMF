package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.HibernateTestCase;

import java.util.List;

public class UsersDaoTest extends HibernateTestCase {

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldFetchAllUsers() {
        UsersDao dao = new UsersDao();

        List users = dao.getAll(session);
        assertTrue("Should contain at least 2 users", users.size() >= 2);
    }
}
