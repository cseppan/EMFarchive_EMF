package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

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
        Scc[] sccs = { new Scc("100232", "") };
        try {
            dao.add(cm, sccs, session);
            ControlMeasure result = load(cm);

            assertEquals(cm.getId(), result.getId());
            assertEquals(cm.getName(), result.getName());
        } finally {
            remove(sccs[0]);
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
            assertEquals("cm one", ((ControlMeasure) cms.get(0)).getName());
        } finally {
            remove(cm);
        }
    }

    public void testShouldUpdateControlMeasure() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            ControlMeasure[] allMeasures = (ControlMeasure[]) dao.all(session).toArray(new ControlMeasure[0]);
            assertEquals(1, allMeasures.length);

            allMeasures[0].setName("updated-name");
            allMeasures[0].setDescription("updated-description");

            dao.update(allMeasures, session);
            session.clear();// to ensure Hibernate does not return cached objects

            ControlMeasure[] updated = (ControlMeasure[]) dao.all(session).toArray(new ControlMeasure[0]);
            assertEquals(1, updated.length);
            assertEquals("updated-name", updated[0].getName());
            assertEquals("updated-description", updated[0].getDescription());
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

            ControlMeasure[] read = (ControlMeasure[]) dao.all(session).toArray(new ControlMeasure[0]);
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
        User owner = emfUser();
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            ControlMeasure locked = dao.obtainLocked(owner, cm, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());
            locked.setName("TEST");

            ControlMeasure modified = dao.update(locked,new Scc[]{},session);
            assertEquals("TEST", locked.getName());
            assertEquals(modified.getLockOwner(), null);
        } finally {
            remove(cm);
        }
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        User owner = emfUser();
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        try {
            dao.obtainLocked(owner, cm, session);

            UserDAO userDao = new UserDAO();
            User user = userDao.get("admin", session);
            ControlMeasure result = dao.obtainLocked(user, cm, session);

            assertFalse("Should have failed to obtain lock as it's already locked by another user", result
                    .isLocked(user));// failed to obtain lock for another user
        } finally {
            remove(cm);
        }
    }

    public void testShouldReleaseLock() {
        User owner = emfUser();
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

    private User emfUser() {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        return owner;
    }

    public void testShouldAddTwoEfficiencyRecordForExistingControlMeasure() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        User emfUser = emfUser();
        cm.setCreator(emfUser);

        try {
            dao.add(cm, cm.getSccs(), session);
            cm = dao.obtainLocked(emfUser, cm, session);
            EfficiencyRecord record1 = efficiencyRecord(pm10Pollutant(), "22");
            EfficiencyRecord record2 = efficiencyRecord(pm10Pollutant(), "22");
            cm.setEfficiencyRecords(new EfficiencyRecord[] { record1, record2 });
            dao.update(cm,cm.getSccs(),session);
            ControlMeasure newMeasure = load(cm);
            EfficiencyRecord[] efficiencyRecords = newMeasure.getEfficiencyRecords();
            assertEquals(2, efficiencyRecords.length);
            assertEquals(record1.getPollutant(), efficiencyRecords[0].getPollutant());
            assertEquals(record1.getLocale(), efficiencyRecords[0].getLocale());
            assertEquals(record2.getPollutant(), efficiencyRecords[1].getPollutant());
            assertEquals(record2.getLocale(), efficiencyRecords[1].getLocale());
        } finally {
            remove(cm);
        }
    }

    private EfficiencyRecord efficiencyRecord(Pollutant pollutant, String locale) {
        EfficiencyRecord record = new EfficiencyRecord();
        record.setPollutant(pollutant);
        record.setLocale(locale);
        return record;
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

    private Pollutant pm10Pollutant() {
        return (Pollutant) load(Pollutant.class, "PM10");
    }

}
