package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DataCommonsDAOTest extends ServicesTestCase {

    private DataCommonsDAO dao;

    private UserDAO userDao;

    private Session session;

    private HibernateSessionFactory sessionFactory;

    protected void doSetUp() throws Exception {
        sessionFactory = new HibernateSessionFactory(sessionFactory());
        dao = new DataCommonsDAO();
        userDao = new UserDAO();
        session = sessionFactory.getSession();
    }

    protected void doTearDown() throws Exception {// no op
        session.close();
    }

    public void testShouldGetAllSectors() {
        List sectors = dao.getSectors(session);
        assertTrue(sectors.size() >= 14);
    }

    public void testShouldGetAllDatasetTypes() {
        List types = dao.getDatasetTypes(session);
        assertTrue(types.size() >= 10);
    }

    private Sector currentSector(Sector target) {
        List sectors = dao.getSectors(session);
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector element = (Sector) iter.next();
            if (element.equals(target))
                return element;
        }

        return null;
    }

    private DatasetType currentDatasetType(DatasetType target) {
        List list = dao.getDatasetTypes(session);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            DatasetType element = (DatasetType) iter.next();
            if (element.equals(target))
                return element;
        }

        return null;
    }

    public void testShouldGetSectorLock() {
        User user = userDao.get("emf", session);
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        Sector lockedSector = dao.obtainLockedSector(user, sector, session);
        assertEquals(lockedSector.getLockOwner(), user.getUsername());

        Sector sectorLoadedFromDb = currentSector(sector);
        assertEquals(sectorLoadedFromDb.getLockOwner(), user.getUsername());
    }

    public void testShouldGetDatasetTypeLock() {
        User owner = userDao.get("emf", session);
        List types = dao.getDatasetTypes(session);
        DatasetType type = (DatasetType) types.get(0);

        DatasetType locked = dao.obtainLockedDatasetType(owner, type, session);
        assertEquals(locked.getLockOwner(), owner.getUsername());

        DatasetType loadedFromDb = currentDatasetType(type);
        assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        User owner = userDao.get("emf", session);
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        dao.obtainLockedSector(owner, sector, session);

        User user = userDao.get("admin", session);
        Sector result = dao.obtainLockedSector(user, sector, session);

        assertTrue(result.isLocked(owner));// locked by owner
        assertFalse(result.isLocked(user));// failed to obtain lock for another user
    }

    public void testShouldReleaseSectorLock() throws EmfException {
        User owner = userDao.get("emf", session);
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        Sector lockedSector = dao.obtainLockedSector(owner, sector, session);
        Sector releasedSector = dao.releaseLockedSector(lockedSector, session);
        assertFalse("Should have released lock", releasedSector.isLocked());

        Sector sectorLoadedFromDb = currentSector(sector);
        assertFalse("Should have released lock", sectorLoadedFromDb.isLocked());
    }

    public void testShouldReleaseDatasetTypeLock() throws EmfException {
        User owner = userDao.get("emf", session);
        List list = dao.getDatasetTypes(session);
        DatasetType type = (DatasetType) list.get(0);

        DatasetType locked = dao.obtainLockedDatasetType(owner, type, session);
        DatasetType released = dao.releaseLockedDatasetType(locked, session);
        assertFalse("Should have released lock", released.isLocked());

        DatasetType loadedFromDb = currentDatasetType(type);
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    public void testShouldFailToReleaseSectorLockIfNotObtained() {
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        try {
            dao.releaseLockedSector(sector, session);
        } catch (EmfException e) {
            assertEquals("Cannot release without owning lock", e.getMessage());
            return;
        }

        fail("Should have failed to release lock that was not obtained");
    }

    public void testShouldUpdateSector() throws EmfException {
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);
        String name = sector.getName();

        User owner = userDao.get("emf", session);

        Sector modifiedSector1 = dao.obtainLockedSector(owner, sector, session);
        assertEquals(modifiedSector1.getLockOwner(), owner.getUsername());
        modifiedSector1.setName("TEST");

        Sector modifiedSector2 = dao.updateSector(modifiedSector1, session);
        assertEquals("TEST", modifiedSector1.getName());
        assertEquals(modifiedSector2.getLockOwner(), null);

        // restore
        Sector modifiedSector = dao.obtainLockedSector(owner, sector, session);
        modifiedSector.setName(name);

        Sector modifiedSector3 = dao.updateSector(modifiedSector, session);
        assertEquals(sector.getName(), modifiedSector3.getName());
    }

    public void testShouldUpdateDatasetType() throws EmfException {
        List types = dao.getDatasetTypes(session);
        DatasetType type = (DatasetType) types.get(0);
        String name = type.getName();

        User owner = userDao.get("emf", session);

        DatasetType modified1 = dao.obtainLockedDatasetType(owner, type, session);
        assertEquals(modified1.getLockOwner(), owner.getUsername());
        modified1.setName("TEST");

        DatasetType modified2 = dao.updateDatasetType(modified1, session);
        assertEquals("TEST", modified1.getName());
        assertEquals(modified2.getLockOwner(), null);

        // restore
        DatasetType modified = dao.obtainLockedDatasetType(owner, type, session);
        modified.setName(name);

        DatasetType modified3 = dao.updateDatasetType(modified, session);
        assertEquals(type.getName(), modified3.getName());
    }

    public void testShouldGetAllStatusMessages() {
        User emf = userDao.get("emf", session);
        Status status = newStatus(emf);

        try {
            List messages = dao.allStatus(emf.getUsername(), session);
            assertEquals(1, messages.size());
        } finally {
            remove(status);
        }
    }

    private Status newStatus(User emf) {
        Status status = unreadStatus(emf);

        add(status);

        return status(status.getMessage());
    }

    private Status unreadStatus(User emf) {
        Status status = new Status();
        status.setUsername(emf.getUsername());
        status.setMessageType("type");
        status.setMessage("message");
        status.setTimestamp(new Date());
        return status;
    }

    public void testShouldClearPreviousReadMessagesOnGetAll() {
        User emf = userDao.get("emf", session);
        newReadStatus(emf);

        List messages = dao.allStatus(emf.getUsername(), session);
        assertEquals(0, messages.size());
    }

    public void testShouldPersistStatusOnAdd() {
        User emf = userDao.get("emf", session);
        Status status = unreadStatus(emf);

        dao.add(status, session);

        try {
            List messages = dao.allStatus(emf.getUsername(), session);
            assertEquals(1, messages.size());
        } finally {
            remove(status);
        }
    }

    private Status newReadStatus(User emf) {
        Status status = new Status();
        status.setUsername(emf.getUsername());
        status.setMessageType("type");
        status.setMessage("message");
        status.setTimestamp(new Date());

        status.setMsgRead();

        add(status);

        return status(status.getMessage());
    }

    private Status status(String message) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("message", message));
            tx.commit();

            return (Status) crit.uniqueResult();
        } catch (HibernateException e) {
            e.printStackTrace();
            tx.rollback();
            throw e;
        }
    }

    private void add(Status status) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(status);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void remove(Status status) {
        Transaction tx = session.beginTransaction();
        session.delete(status);
        tx.commit();
    }

}
