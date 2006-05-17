package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class ControlMeasureDaoTest extends ServiceTestCase {

    private ControlMeasuresDAO dao;
    
    private UserDAO userDAO;

    protected void doSetUp() throws Exception {
        dao = new ControlMeasuresDAO();
        userDAO = new UserDAO();
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetAll() {
        List all = dao.all(session);
        assertEquals(0, all.size());
    }

    public void testShouldAddControlMeasureToDatabaseOnAdd() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        try {
            dao.add(cm, session);
            ControlMeasure result = load(cm);
            
            assertEquals(cm.getId(), result.getId());
            assertEquals(cm.getName(), result.getName());
        } finally {
            remove(cm);
        }
    }

    public void testShouldUpdateControlMeasureOnUpdate() throws Exception {
        ControlMeasure cm = new ControlMeasure();

        try {
            cm.setName("cm one modified");
            cm.setAbbreviation("12345678");
            dao.updateWithoutLocking(cm, session);
            ControlMeasure result = load(cm);

            assertEquals(cm.getId(), result.getId());
            assertEquals("cm one modified", result.getName());
        } finally {
            remove(cm);
        }
    }

    public void testShouldGetControlMeasures() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            List cms = dao.all(session);

            assertEquals(1, cms.size());
            assertEquals("cm one", ((ControlMeasure)cms.get(0)).getName());
        } finally {
            remove(cm);
        }
    }

    public void testShouldUpdateQASteps() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            ControlMeasure[] read = (ControlMeasure[])dao.all(session).toArray(new ControlMeasure[0]);
            assertEquals(1, read.length);

            read[0].setName("updated-name");
            read[0].setDescription("updated-description");

            dao.update(read, session);
            session.clear();// to ensure Hibernate does not return cached objects

            ControlMeasure[] updated = (ControlMeasure[])dao.all(session).toArray(new ControlMeasure[0]);
            assertEquals(1, updated.length);
            assertEquals("updated-name", updated[0].getName());
            assertEquals("updated-description", updated[0].getDescription());
        } finally {
            remove(cm);
        }
    }

    public void testShouldAddNewControlMeasuresOnUpdateQASteps() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");

        try {
            dao.update(new ControlMeasure[] { cm }, session);
            session.clear();// to ensure Hibernate does not return cached objects

            ControlMeasure[] updated = (ControlMeasure[])dao.all(session).toArray(new ControlMeasure[0]);
            assertEquals(1, updated.length);
            assertEquals("cm one", updated[0].getName());
        } finally {
            remove(cm);
        }
    }

    public void testShouldSaveNewControlMeasures() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");

        try {
            dao.add(new ControlMeasure[] { cm }, session);
            session.clear();

            ControlMeasure[] read = (ControlMeasure[])dao.all(session).toArray(new ControlMeasure[0]);
            assertEquals(1, read.length);
            assertEquals("cm one", read[0].getName());
            assertEquals(cm.getId(), read[0].getId());
        } finally {
            remove(cm);
        }
    }

    public void testShouldRemoveControlMeasureFromDatabaseOnRemove() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");

        add(cm);

        dao.remove(cm, session);
        ControlMeasure result = load(cm);

        assertNull("Should be removed from the database on 'remove'", result);
    }

    public void testShouldConfirmControlMeasureExistsWhenQueriedByName() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            assertTrue("Should be able to confirm existence of dataset", dao.exists(cm.getName(), session));

        } finally {
            remove(cm);
        }
    }

    public void testShouldObtainLockedControlMeasureForUpdate() {
        User owner = userDAO.get("emf", session);
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            ControlMeasure locked = dao.obtainLocked(owner, cm, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());

            ControlMeasure loadedFromDb = load(cm);
            assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
        } finally {
            remove(cm);
        }
    }

    public void testShouldUpdateControlMeasureAfterObtainingLock() throws EmfException {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            ControlMeasure locked = dao.obtainLocked(owner, cm, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());
            locked.setName("TEST");

            ControlMeasure modified = dao.update(locked, session);
            assertEquals("TEST", locked.getName());
            assertEquals(modified.getLockOwner(), null);
        } finally {
            remove(cm);
        }
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            dao.obtainLocked(owner, cm, session);

            User user = userDao.get("admin", session);
            ControlMeasure result = dao.obtainLocked(user, cm, session);

            assertFalse("Should have failed to obtain lock as it's already locked by another user", result
                    .isLocked(user));// failed to obtain lock for another user
        } finally {
            remove(cm);
        }
    }

    public void testShouldReleaseLock() {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            ControlMeasure locked = dao.obtainLocked(owner, cm, session);
            ControlMeasure released = dao.releaseLocked(locked, session);
            assertFalse("Should have released lock", released.isLocked());

            ControlMeasure loadedFromDb = load(cm);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(cm);
        }
    }

    private ControlMeasure load(ControlMeasure cm) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(ControlMeasure.class).add(Restrictions.eq("name", cm.getName()));
            tx.commit();

            return (ControlMeasure) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
