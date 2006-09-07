package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;

import java.util.List;

public class DatasetDaoTest extends ServiceTestCase {

    private DatasetDAO dao;

    private DataCommonsDAO dcDao;

    private UserDAO userDAO;

    protected void doSetUp() throws Exception {
        deleteAllDatasets();
        dao = new DatasetDAO();
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
        EmfDataset dataset = newDataset("dataset-dao-test");
        try {
            dao.add(dataset, session);
            EmfDataset result = (EmfDataset) load(EmfDataset.class, dataset.getName());

            assertEquals(dataset.getId(), result.getId());
            assertEquals(dataset.getName(), result.getName());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldGetDatasetsForaDatasetType() throws Exception {
        DatasetType datasetType1 = newDatasetType("type1");
        DatasetType datasetType2 = newDatasetType("type2");

        EmfDataset dataset1 = newDataset("name1");
        EmfDataset dataset2 = newDataset("name2");
        EmfDataset dataset3 = newDataset("name3");
        
        dataset1.setDatasetType(datasetType1);
        dataset2.setDatasetType(datasetType1);
        dataset3.setDatasetType(datasetType2);

        try {
            dao.updateWithoutLocking(dataset1,session);
            dao.updateWithoutLocking(dataset2,session);
            dao.updateWithoutLocking(dataset3,session);
            
            List datasets = dao.getDatasets(session, datasetType1);
            assertEquals(2, datasets.size());
            assertEquals(dataset1.getId(), ((EmfDataset) datasets.get(0)).getId());
            assertEquals(dataset1.getName(), ((EmfDataset) datasets.get(0)).getName());
            assertEquals(dataset2.getId(), ((EmfDataset) datasets.get(1)).getId());
            assertEquals(dataset2.getName(), ((EmfDataset) datasets.get(1)).getName());
        } finally {
            remove(dataset1);
            remove(dataset2);
            remove(dataset3);
            remove(datasetType1);
            remove(datasetType2);
        }
    }

    public void testShouldUpdateDatasetOnUpdate() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");
        Country country = new Country("test-country");

        try {
            dcDao.add(country, session);
            dataset.setCountry(country);

            dao.updateWithoutLocking(dataset, session);
            EmfDataset result = (EmfDataset) load(EmfDataset.class, dataset.getName());

            assertEquals(dataset.getId(), result.getId());
            assertEquals("test-country", result.getCountry().getName());
        } finally {
            remove(dataset);
            remove(country);
        }
    }


    public void testShouldRemoveDatasetFromDatabaseOnRemove() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        dao.remove(dataset, session);
        EmfDataset result = (EmfDataset) load(EmfDataset.class, dataset.getName());

        assertNull("Should be removed from the database on 'remove'", result);
    }

    public void testShouldConfirmDatasetExistsWhenQueriedByName() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        try {
            assertTrue("Should be able to confirm existence of dataset", dao.exists(dataset.getName(), session));

        } finally {
            remove(dataset);
        }
    }

    public void testShouldObtainLockedDatasetForUpdate() {
        User owner = userDAO.get("emf", session);
        EmfDataset dataset = newDataset("dataset-dao-test");

        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());

            EmfDataset loadedFromDb = (EmfDataset) load(EmfDataset.class, dataset.getName());
            assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldUpdateDatasetAfterObtainingLock() throws EmfException {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        EmfDataset dataset = newDataset("dataset-dao-test");

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
        EmfDataset dataset = newDataset("dataset-dao-test");

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

    public void testShouldReleaseLock() {
        UserDAO userDao = new UserDAO();
        User owner = userDao.get("emf", session);
        EmfDataset dataset = newDataset("dataset-dao-test");

        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            EmfDataset released = dao.releaseLocked(locked, session);
            assertFalse("Should have released lock", released.isLocked());

            EmfDataset loadedFromDb = (EmfDataset) load(EmfDataset.class, dataset.getName());
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(dataset);
        }
    }

    private EmfDataset newDataset(String name) {
        User owner = userDAO.get("emf", session);

        EmfDataset dataset = new EmfDataset();
        dataset.setName(name);
        dataset.setCreator(owner.getUsername());

        save(dataset);
        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

    private DatasetType newDatasetType(String name) {
        DatasetType datasetType = new DatasetType(name);
        datasetType.setDescription("no description");
        save(datasetType);

        return (DatasetType) load(DatasetType.class, datasetType.getName());
    }

}

