package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;

import java.util.List;

public class DatasetDaoTest extends ServiceTestCase {

    private DatasetDAO dao;

    private DataCommonsDAO dcDao;

    private UserDAO userDAO;
    
    private DataCommonsDAO dataDAO;
    
    private ControlStrategyDAO strategyDao;
    
    private CaseDAO caseDao;
    
    private DatasetType[] types;

    protected void doSetUp() throws Exception {
        deleteAllDatasets();
        dao = new DatasetDAO();
        dcDao = new DataCommonsDAO();
        userDAO = new UserDAO();
        dataDAO = new DataCommonsDAO();
        strategyDao = new ControlStrategyDAO();
        caseDao = new CaseDAO();
        types = (DatasetType[])dataDAO.getDatasetTypes(session).toArray(new DatasetType[0]);

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

    public void testShouldGetNonDeletedDatasets() {
        EmfDataset dataset = newDataset("dataset-dao-test");
        EmfDataset dataset2 = newDataset("test2");
        dataset2.setStatus("Deleted");
        EmfDataset dataset3 = newDataset("test3");
        dataset3.setStatus("Deleted");
        
        try {
            dao.updateWithoutLocking(dataset, session);
            dao.updateWithoutLocking(dataset2, session);
            dao.updateWithoutLocking(dataset3, session);
            
            EmfDataset[] loadedFromDb = (EmfDataset[])dao.allNonDeleted(session).toArray(new EmfDataset[0]);
            assertEquals(1, loadedFromDb.length);
            assertEquals("dataset-dao-test", loadedFromDb[0].getName());
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
        }
    }

    public void testShouldGetNonDeletedDatasetsWithSpecifiedDatasettype() {
        EmfDataset dataset = newDataset("dataset-dao-test");
        dataset.setDatasetType(types[0]);
        EmfDataset dataset2 = newDataset("test2");
        dataset2.setDatasetType(types[1]);
        EmfDataset dataset3 = newDataset("test3");
        dataset3.setDatasetType(types[0]);
        dataset3.setStatus("Deleted");
        
        try {
            dao.updateWithoutLocking(dataset, session);
            dao.updateWithoutLocking(dataset2, session);
            dao.updateWithoutLocking(dataset3, session);
            
            EmfDataset[] loadedFromDb = (EmfDataset[])dao.getDatasets(session, types[0]).toArray(new EmfDataset[0]);
            assertEquals(1, loadedFromDb.length);
            assertEquals("dataset-dao-test", loadedFromDb[0].getName());
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
        }
    }

    public void testShouldGetNonDeletedDatasetsWithSpecifiedName() {
        EmfDataset dataset = newDataset("dataset-dao-test");
        EmfDataset dataset2 = newDataset("test2");
        dataset2.setStatus("Deleted");
        EmfDataset dataset3 = newDataset("test3");
        dataset3.setStatus("Deleted");
        
        try {
            dao.updateWithoutLocking(dataset, session);
            dao.updateWithoutLocking(dataset2, session);
            dao.updateWithoutLocking(dataset3, session);
            
            EmfDataset loadedFromDb = dao.getDataset(session, "dataset-dao-test");
            assertEquals("dataset-dao-test", loadedFromDb.getName());
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
        }
    }

    public void testShouldGetNonDeletedDatasetsWithSpecifiedID() {
        EmfDataset dataset = newDataset("dataset-dao-test");
        EmfDataset dataset2 = newDataset("test2");
        dataset2.setStatus("Deleted");
        EmfDataset dataset3 = newDataset("test3");
        dataset3.setStatus("Deleted");
        
        try {
            dao.updateWithoutLocking(dataset, session);
            dao.updateWithoutLocking(dataset2, session);
            dao.updateWithoutLocking(dataset3, session);
            
            EmfDataset loadedFromDb = dao.getDataset(session, dataset.getId());
            assertEquals("dataset-dao-test", loadedFromDb.getName());

            EmfDataset loadedFromDb2 = dao.getDataset(session, dataset2.getId());
            assertNull(loadedFromDb2);

            EmfDataset loadedFromDb3 = dao.getDataset(session, dataset3.getId());
            assertNull(loadedFromDb3);
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
        }
    }

    public void testShouldDetectWhetherUsedByControlStrategiesOrCases() {
        EmfDataset dataset = newDataset("dataset-dao-test");
        EmfDataset dataset2 = newDataset("test2");
        EmfDataset dataset3 = newDataset("test3");
        
        ControlStrategy strategy = newControlStrategy();
        strategy.setInputDatasets(new EmfDataset[]{dataset});
        
        CaseInput input = new CaseInput();
        input.setDataset(dataset2);
        Case caseObj = newCase();
        caseObj.setCaseInputs(new CaseInput[]{input});
        
        
        try {
            assertTrue(dao.isUsedByControlStrategies(session, dataset));
            assertFalse(dao.isUsedByControlStrategies(session, dataset2));
            assertFalse(dao.isUsedByControlStrategies(session, dataset3));
            
            assertTrue(dao.isUsedByCases(session, dataset2));
            assertFalse(dao.isUsedByCases(session, dataset));
            assertFalse(dao.isUsedByCases(session, dataset3));
        } finally {
            remove(dataset);
            remove(dataset2);
            remove(dataset3);
            remove(strategy);
            remove(input);
            remove(caseObj);
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
    
    private ControlStrategy newControlStrategy() {
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        strategyDao.add(element, session);
        return element;
    }
    
    private Case newCase() {
        Case element = new Case("test" + Math.random());
        caseDao.add(element, session);

        return element;
    }

}

