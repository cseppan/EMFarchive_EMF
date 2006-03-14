package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.DatasetDao;
import gov.epa.emissions.framework.services.persistence.UserDAO;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DatasetDaoTest extends ServiceTestCase {

    private DatasetDao dao;

    private DataCommonsDAO dcDao;

    private UserDAO userDAO;

    protected void doSetUp() throws Exception {
        deleteAllDatasets();
        dao = new DatasetDao();
        dcDao = new DataCommonsDAO();
        userDAO = new UserDAO();
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetAll() {
        List all = dao.all(session);
        assertEquals(0, all.size());
    }

    public void testShouldAddDatasetToDatabaseOnAdd() throws Exception {
        EmfDataset dataset = newDataset();
        try {
            dao.add(dataset, session);
            EmfDataset result = load(dataset);

            assertEquals(dataset.getId(), result.getId());
            assertEquals(dataset.getName(), result.getName());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldUpdateDatasetOnUpdate() throws Exception {
        EmfDataset dataset = newDataset();
        Country country = new Country("test-country");

        try {
            dcDao.add(country, session);
            dataset.setCountry(country);

            dao.updateWithoutLocking(dataset, session);
            EmfDataset result = load(dataset);

            assertEquals(dataset.getId(), result.getId());
            assertEquals("test-country", result.getCountry().getName());
        } finally {
            remove(dataset);
            remove(country);
        }
    }

    public void testShouldRemoveDatasetFromDatabaseOnRemove() throws Exception {
        EmfDataset dataset = newDataset();

        dao.remove(dataset, session);
        EmfDataset result = load(dataset);

        assertNull("Should be removed from the database on 'remove'", result);
    }

    public void testShouldConfirmDatasetExistsWhenQueriedByName() throws Exception {
        EmfDataset dataset = newDataset();

        try {
            assertTrue("Should be able to confirm existence of dataset", dao.exists(dataset.getName(), session));
            
        } finally {
            remove(dataset);
        }
    }

    public void testShouldObtainLockedDatasetForUpdate() {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());

            EmfDataset loadedFromDb = load(dataset);
            assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldUpdateDatasetAfterObtainingLock() throws EmfException {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());
            locked.setName("TEST");

            EmfDataset modified = dao.update(locked, session);
            assertEquals("TEST", locked.getName());
            assertEquals(modified.getLockOwner(), null);
        } finally {
            remove(dataset);
        }
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        EmfDataset dataset = newDataset();

        try {
            dao.obtainLocked(owner, dataset, session);

            User user = userDao.get("admin", session);
            EmfDataset result = dao.obtainLocked(user, dataset, session);

            assertFalse("Should have failed to obtain lock as it's already locked by another user", result
                    .isLocked(user));// failed to obtain lock for another user
        } finally {
            remove(dataset);
        }
    }

    public void testShouldReleaseLock() throws EmfException {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            EmfDataset released = dao.releaseLocked(locked, session);
            assertFalse("Should have released lock", released.isLocked());

            EmfDataset loadedFromDb = load(dataset);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldFailToReleaseLockIfNotObtained() {
        EmfDataset dataset = newDataset();

        try {
            dao.releaseLocked(dataset, session);
        } catch (EmfException e) {
            assertEquals("Cannot release without owning lock", e.getMessage());
            return;
        } finally {
            remove(dataset);
        }

        fail("Should have failed to release lock that was not obtained");
    }

    private EmfDataset newDataset() {
        User owner = userDAO.get("emf", session);

        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset-dao-test");
        dataset.setCreator(owner.getUsername());

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(dataset);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return load(dataset);
    }

    private EmfDataset load(EmfDataset dataset) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfDataset.class).add(Restrictions.eq("name", dataset.getName()));
            tx.commit();

            return (EmfDataset) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
