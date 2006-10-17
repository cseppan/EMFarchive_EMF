package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class ControlMeasureDaoTest extends ServiceTestCase {

    private ControlMeasureDAO dao;

    private UserDAO userDAO;

    protected void doSetUp() throws Exception {
        dao = new ControlMeasureDAO();
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

            ControlMeasure modified = dao.update(locked, new Scc[] {}, session);
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
            dao.update(cm, cm.getSccs(), session);
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

    public void testShouldOverwriteCM_WhenImport_WithSameNameAndSameAbbrev() throws HibernateException,
            Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        cm.setCmClass("Experiment");
        User emfUser = emfUser();
        cm.setCreator(emfUser);

        EfficiencyRecord[] records1 = { efficiencyRecord(pm10Pollutant(), "01"),
                efficiencyRecord(pm10Pollutant(), "23") };
        Scc[] sccs1 = sccs("10031");
        cm.setEfficiencyRecords(records1);

        EfficiencyRecord[] records2 = { efficiencyRecord(COPollutant(), "32") };
        Scc[] sccs2 = sccs("10231");
        try {
            addCMFromImporter(cm, emfUser, sccs1);

            cm.setCmClass("Theory");
            cm.setEfficiencyRecords(records2);
            addCMFromImporter(cm, emfUser, sccs2);

            ControlMeasure controlMeasure = (ControlMeasure) load(ControlMeasure.class, "cm one");
            assertEquals("Theory", controlMeasure.getCmClass());

            EfficiencyRecord[] efficiencyRecords = controlMeasure.getEfficiencyRecords();
            assertEquals(1, efficiencyRecords.length);
            assertEquals("32", efficiencyRecords[0].getLocale());

            Scc[] sccs = loadSccs(controlMeasure);

            assertEquals(1, sccs.length);
            assertEquals("10231", sccs[0].getCode());
        } finally {
            dropAll(Scc.class);
            dropAll(ControlMeasure.class);
        }

    }
    
    public void testShouldOverwriteCM_WhenImport_WithSameNameAndDifferenAbbrev() throws EmfException, Exception{
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        cm.setCmClass("Experiment");
        User emfUser = emfUser();
        cm.setCreator(emfUser);

        EfficiencyRecord[] records1 = { efficiencyRecord(pm10Pollutant(), "01"),
                efficiencyRecord(pm10Pollutant(), "23") };
        Scc[] sccs1 = sccs("10031");
        cm.setEfficiencyRecords(records1);

        EfficiencyRecord[] records2 = { efficiencyRecord(COPollutant(), "32")};
        Scc[] sccs2 = sccs("10231");
        try {
            addCMFromImporter(cm, emfUser, sccs1);
            
            cm.setAbbreviation("UNCCEP");
            cm.setCmClass("Theory");
            cm.setEfficiencyRecords(records2);
            addCMFromImporter(cm, emfUser, sccs2);
            
            ControlMeasure controlMeasure = (ControlMeasure) load(ControlMeasure.class,"cm one");
            assertEquals("UNCCEP",controlMeasure.getAbbreviation());
            assertEquals("Theory",controlMeasure.getCmClass());

            EfficiencyRecord[] efficiencyRecords = controlMeasure.getEfficiencyRecords();
            assertEquals(1,efficiencyRecords.length);
            assertEquals("32",efficiencyRecords[0].getLocale());
            
            Scc[] sccs = loadSccs(controlMeasure);
            
            assertEquals(1,sccs.length);
            assertEquals("10231",sccs[0].getCode());
        } finally {
            dropAll(Scc.class);
            dropAll(ControlMeasure.class);
        }

    
    }
    
    public void testShouldOverwriteCM_WhenImport_WithDifferentNameAndSameAbbrev() throws Exception{
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        cm.setCmClass("Experiment");
        User emfUser = emfUser();
        cm.setCreator(emfUser);

        EfficiencyRecord[] records1 = { efficiencyRecord(pm10Pollutant(), "01"),
                efficiencyRecord(pm10Pollutant(), "23") };
        Scc[] sccs1 = sccs("10031");
        cm.setEfficiencyRecords(records1);

        EfficiencyRecord[] records2 = { efficiencyRecord(COPollutant(), "32")};
        Scc[] sccs2 = sccs("10231");
        try {
            addCMFromImporter(cm, emfUser, sccs1);
            cm.setName("cm two");
            cm.setCmClass("Theory");
            cm.setEfficiencyRecords(records2);
            addCMFromImporter(cm, emfUser, sccs2);
            assertFalse("Should have throw an exception: abbrev exist",true);
        }catch (Exception e) {
            assertEquals("The Control Measure Abbreviation already in use: 12345678",e.getMessage());
            assertTrue("The exception is thrown: abbrev exist",true);
        } finally {
            dropAll(Scc.class);
            dropAll(ControlMeasure.class);
        }

    
    }
    
    

    private Scc[] loadSccs(ControlMeasure controlMeasure) throws HibernateException, Exception {
        HibernateFacade facade = new  HibernateFacade();
        Criterion c1 = Restrictions.eq("controlMeasureId",new Integer(controlMeasure.getId()));
        Session session = sessionFactory().getSession();
        try{
            List list = facade.get(Scc.class,c1,session);
            return (Scc[]) list.toArray(new Scc[0]);
        }finally{
            session.close();
        }
    }

    private void addCMFromImporter(ControlMeasure cm, User emfUser, Scc[] sccs) throws Exception, EmfException {
        Session session = sessionFactory().getSession();
        try {
            dao.addFromImporter(cm, sccs, emfUser, session);
        } finally {
            session.close();
        }
    }

    private Scc[] sccs(String name) {
        Scc scc = new Scc(name, "");
        return new Scc[] { scc };
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

    private Pollutant COPollutant() {
        return (Pollutant) load(Pollutant.class, "CO");
    }

}
