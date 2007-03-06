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

    protected void doSetUp() throws Exception {
        dao = new ControlMeasureDAO();
    }

    protected void doTearDown() throws Exception {// no op
        dropAll(Scc.class);
        dropAll(ControlMeasure.class);
    }

    public void testShouldGetAll() {
        List all = dao.all(session);
        assertEquals(0, all.size());
    }

    public void testShouldGetAllLightCM() {
        List all = dao.getLightControlMeasures(session);
        assertEquals(0, all.size());
    }

    public void testShouldAddControlMeasureToDatabaseOnAdd() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        Scc[] sccs = { new Scc("100232", "") };
        dao.add(cm, sccs, session);
        ControlMeasure result = load(cm);

        assertEquals(cm.getId(), result.getId());
        assertEquals(cm.getName(), result.getName());
    }

    public void testShouldGetControlMeasures() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        List cms = dao.all(session);

        assertEquals(1, cms.size());
        assertEquals("cm one", ((ControlMeasure) cms.get(0)).getName());
    }

    public void testShouldUpdateControlMeasure() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        User user = emfUser();
        cm.setCreator(user);
        add(cm);
        Scc scc1 = new Scc();
        scc1.setCode("scc1");

        Scc scc2 = new Scc();
        scc2.setCode("scc2");
        Scc[] sccs = { scc1, scc2 };

        ControlMeasure locked = dao.obtainLocked(user, cm.getId(), session);

        locked.setName("updated-name");
        locked.setDescription("updated-description");

        dao.update(locked, sccs, session);
        session.clear();// to ensure Hibernate does not return cached objects

        ControlMeasure[] updated = (ControlMeasure[]) dao.all(session).toArray(new ControlMeasure[0]);
        assertEquals(1, updated.length);
        assertEquals("updated-name", updated[0].getName());
        assertEquals("updated-description", updated[0].getDescription());
        Scc[] updatedSccs = dao.getSccs(updated[0].getId(), session);
        assertEquals(2, updatedSccs.length);
        assertEquals("scc1", updatedSccs[0].getCode());
        assertEquals("scc2", updatedSccs[1].getCode());
        assertEquals(updated[0].getId(), updatedSccs[0].getControlMeasureId());
        assertEquals(updated[0].getId(), updatedSccs[1].getControlMeasureId());

        // checking whether current scc associate with CM is removed and new measures are added
        session.clear();

        Scc scc3 = new Scc();
        scc3.setCode("scc3");

        Scc scc4 = new Scc();
        scc4.setCode("scc4");
        Scc[] newSccs = { scc1,scc3, scc4 };
        locked = dao.obtainLocked(user, cm.getId(), session);
        dao.update(locked, newSccs, session);
        
        ControlMeasure[] newUpdated = (ControlMeasure[]) dao.all(session).toArray(new ControlMeasure[0]);
        updatedSccs = dao.getSccs(newUpdated[0].getId(), session);
        assertEquals(3, updatedSccs.length);
        assertEquals("scc1", updatedSccs[0].getCode());
        assertEquals("scc3", updatedSccs[1].getCode());
        assertEquals("scc4", updatedSccs[2].getCode());
        assertEquals(updated[0].getId(), updatedSccs[0].getControlMeasureId());
        assertEquals(updated[0].getId(), updatedSccs[1].getControlMeasureId());
        assertEquals(updated[0].getId(), updatedSccs[2].getControlMeasureId());


    }

    public void testShouldSaveNewControlMeasures() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");

        dao.add(cm, new Scc[] {}, session);
        session.clear();

        ControlMeasure[] read = (ControlMeasure[]) dao.all(session).toArray(new ControlMeasure[0]);
        assertEquals(1, read.length);
        assertEquals("cm one", read[0].getName());
        assertEquals(cm.getId(), read[0].getId());
    }

    public void testShouldRemoveControlMeasureFromDatabaseOnRemove() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");

        add(cm);

        dao.remove(cm.getId(), session);
        ControlMeasure result = load(cm);

        assertNull("Should be removed from the database on 'remove'", result);
    }

    public void testShouldConfirmControlMeasureExistsWhenQueriedByName() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        assertTrue("Should be able to confirm existence of dataset", dao.exists(cm.getName(), session));

    }

    public void testShouldObtainLockedControlMeasureForUpdate() {
        User owner = emfUser();
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        ControlMeasure locked = dao.obtainLocked(owner, cm.getId(), session);
        assertEquals(locked.getLockOwner(), owner.getUsername());

        ControlMeasure loadedFromDb = load(cm);
        assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        User owner = emfUser();
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        dao.obtainLocked(owner, cm.getId(), session);

        UserDAO userDao = new UserDAO();
        User user = userDao.get("admin", session);
        ControlMeasure result = dao.obtainLocked(user, cm.getId(), session);

        assertFalse("Should have failed to obtain lock as it's already locked by another user", result.isLocked(user));// failed
    }

    public void testShouldReleaseLock() {
        User owner = emfUser();
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        add(cm);

        ControlMeasure locked = dao.obtainLocked(owner, cm.getId(), session);
        dao.releaseLocked(locked.getId(), session);

        ControlMeasure loadedFromDb = load(cm);
        assertFalse("Should have released lock", loadedFromDb.isLocked());
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

        dao.add(cm, cm.getSccs(), session);
        cm = dao.obtainLocked(emfUser, cm.getId(), session);
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
    }

    public void testShouldOverwriteCM_WhenImport_WithSameNameAndSameAbbrev() throws HibernateException, Exception {
        ControlMeasure cm = new ControlMeasure();
        ControlMeasureClass cmc = dao.getCMClass(session, "Known");
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        cm.setCmClass(cmc);
        User emfUser = emfUser();
        cm.setCreator(emfUser);

        EfficiencyRecord[] records1 = { efficiencyRecord(pm10Pollutant(), "01"),
                efficiencyRecord(pm10Pollutant(), "23") };
        Scc[] sccs1 = sccs("10031");
        cm.setEfficiencyRecords(records1);

        EfficiencyRecord[] records2 = { efficiencyRecord(COPollutant(), "32") };
        Scc[] sccs2 = sccs("10231");
        addCMFromImporter(cm, emfUser, sccs1);

        cmc = dao.getCMClass(session, "Emerging");
        cm.setCmClass(cmc);
        
        cm.setEfficiencyRecords(records2);
        addCMFromImporter(cm, emfUser, sccs2);

        ControlMeasure controlMeasure = (ControlMeasure) load(ControlMeasure.class, "cm one");
        assertEquals("Emerging", controlMeasure.getCmClass().getName());

        EfficiencyRecord[] efficiencyRecords = controlMeasure.getEfficiencyRecords();
        assertEquals(1, efficiencyRecords.length);
        assertEquals("32", efficiencyRecords[0].getLocale());

        Scc[] sccs = loadSccs(controlMeasure);

        assertEquals(1, sccs.length);
        assertEquals("10231", sccs[0].getCode());

    }

    public void testShouldOverwriteCM_WhenImport_WithSameNameAndDifferenAbbrev() throws EmfException, Exception {
        ControlMeasure cm = new ControlMeasure();
        ControlMeasureClass cmc = dao.getCMClass(session, "Hypothetical");
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        cm.setCmClass(cmc);
        User emfUser = emfUser();
        cm.setCreator(emfUser);

        EfficiencyRecord[] records1 = { efficiencyRecord(pm10Pollutant(), "01"),
                efficiencyRecord(pm10Pollutant(), "23") };
        Scc[] sccs1 = sccs("10031");
        cm.setEfficiencyRecords(records1);

        EfficiencyRecord[] records2 = { efficiencyRecord(COPollutant(), "32") };
        Scc[] sccs2 = sccs("10231");

        addCMFromImporter(cm, emfUser, sccs1);

        cm.setAbbreviation("UNCCEP");
        cmc = dao.getCMClass(session, "Obselete");
        cm.setCmClass(cmc);
        cm.setEfficiencyRecords(records2);
        addCMFromImporter(cm, emfUser, sccs2);

        ControlMeasure controlMeasure = (ControlMeasure) load(ControlMeasure.class, "cm one");
        assertEquals("UNCCEP", controlMeasure.getAbbreviation());
        assertEquals("Obselete", controlMeasure.getCmClass().getName());

        EfficiencyRecord[] efficiencyRecords = controlMeasure.getEfficiencyRecords();
        assertEquals(1, efficiencyRecords.length);
        assertEquals("32", efficiencyRecords[0].getLocale());

        Scc[] sccs = loadSccs(controlMeasure);

        assertEquals(1, sccs.length);
        assertEquals("10231", sccs[0].getCode());

    }

    public void testShouldOverwriteCM_WhenImport_WithDifferentNameAndSameAbbrev() throws Exception {
        ControlMeasure cm = new ControlMeasure();
        ControlMeasureClass cmc = dao.getCMClass(session, "Hypothetical");
        cm.setName("cm one");
        cm.setAbbreviation("12345678");
        cm.setCmClass(cmc);
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
            cm.setName("cm two");
            cmc = dao.getCMClass(session, "Emerging");
            cm.setCmClass(cmc);
            cm.setEfficiencyRecords(records2);
            addCMFromImporter(cm, emfUser, sccs2);
            assertFalse("Should have throw an exception: abbrev exist", true);
        } catch (Exception e) {
            assertEquals("The Control Measure Abbreviation already in use: 12345678", e.getMessage());
            assertTrue("The exception is thrown: abbrev exist", true);
        }

    }

    private Scc[] loadSccs(ControlMeasure controlMeasure) throws HibernateException, Exception {
        HibernateFacade facade = new HibernateFacade();
        Criterion c1 = Restrictions.eq("controlMeasureId", new Integer(controlMeasure.getId()));
        Session session = sessionFactory().getSession();
        try {
            List list = facade.get(Scc.class, c1, session);
            return (Scc[]) list.toArray(new Scc[0]);
        } finally {
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
