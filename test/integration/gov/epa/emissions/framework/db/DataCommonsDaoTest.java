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

    private HibernateSessionFactory sessionFactory;

    private UserManagerDAO userDao;

    private Session session;

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
        List sectors = dao.getSectors(sessionFactory.getSession());
        assertTrue(sectors.size() >= 14);
    }

    private Sector sectors(long id) {
        List sectors = dao.getSectors(sessionFactory.getSession());
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
        System.out.println(lockedSector.getName());

        // Sector object returned directly from the sector table
        Sector sectorLoadedFromDb = sectors(sector.getId());
        assertEquals(sectorLoadedFromDb.getUsername(), user.getFullName());
    }

}
