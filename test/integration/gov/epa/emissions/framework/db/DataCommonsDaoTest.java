package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DataCommonsDAO;
import gov.epa.emissions.framework.dao.UserManagerDAO;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

public class DataCommonsDaoTest extends ServicesTestCase {

    private DataCommonsDAO dao;

    private UserManagerDAO userDao;

    private Session session;

    private HibernateSessionFactory sessionFactory;

    protected void doSetUp() throws Exception {
        sessionFactory = new HibernateSessionFactory(sessionFactory());
        dao = new DataCommonsDAO();
        userDao = new UserManagerDAO(emf());
        session = sessionFactory.getSession();
    }

    protected void doTearDown() throws Exception {// no op
        session.close();
    }

    public void testShouldReturnCompleteListOfSectors() {
        List sectors = dao.getSectors(session);
        assertTrue(sectors.size() >= 14);
    }

    private Sector sectors(long id) {
        List sectors = dao.getSectors(session);
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector element = (Sector) iter.next();
            if (element.getId() == id)
                return element;
        }

        return null;
    }

    public void testShouldGetSectorLock() throws EmfException {
        User user = userDao.getUser("emf");
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        Sector lockedSector = dao.getSectorLock(user, sector, session);
        assertEquals(lockedSector.getUsername(), user.getFullName());

        Sector sectorLoadedFromDb = sectors(sector.getId());
        assertEquals(sectorLoadedFromDb.getUsername(), user.getFullName());
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() throws EmfException {
        User emfUser = userDao.getUser("emf");
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        dao.getSectorLock(emfUser, sector, session);

        User adminUser = userDao.getUser("admin");
        Sector result = dao.getSectorLock(adminUser, sector, session);

        assertTrue(result.isLocked(emfUser));// failed to obtain lock for Admin
    }

    public void testShouldReleaseSectorLock() throws EmfException {
        User user = userDao.getUser("emf");
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        Sector lockedSector = dao.getSectorLock(user, sector, session);
        Sector releasedSector = dao.releaseSectorLock(lockedSector, session);
        assertFalse("Should have released lock", releasedSector.isLocked());

        Sector sectorLoadedFromDb = sectors(sector.getId());
        assertFalse("Should have released lock", sectorLoadedFromDb.isLocked());
    }

    public void testShouldFailToReleaseSectorLockIfNotObtained() {
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        try {
            dao.releaseSectorLock(sector, session);
        } catch (EmfException e) {
            assertEquals("Cannot update without owning lock", e.getMessage());
            return;
        }

        fail("Should have failed to release lock that was not obtained");
    }

    public void testShouldUpdateSector() throws EmfException {
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);
        String name = sector.getName();

        User user = userDao.getUser("emf");

        Sector modifiedSector1 = dao.getSectorLock(user, sector, session);
        assertEquals(modifiedSector1.getUsername(), user.getFullName());
        modifiedSector1.setName("TEST");

        Sector modifiedSector2 = dao.updateSector(user, modifiedSector1, session);
        assertEquals("TEST", modifiedSector1.getName());
        assertEquals(modifiedSector2.getUsername(), null);

        // restore
        Sector modifiedSector = dao.getSectorLock(user, sector, session);
        modifiedSector.setName(name);

        Sector modifiedSector3 = dao.updateSector(user, modifiedSector, session);
        assertEquals(sector.getName(), modifiedSector3.getName());
    }
}
