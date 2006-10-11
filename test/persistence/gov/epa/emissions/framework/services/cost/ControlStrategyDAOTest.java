package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.List;

public class ControlStrategyDAOTest extends ServiceTestCase {

    private ControlStrategyDAO dao;

    protected void doSetUp() throws Exception {
        dao = new ControlStrategyDAO();
    }

    protected void doTearDown() throws Exception {
        // no op
    }

    public void testShouldPersistEmptyControlStrategyOnAdd() {
        int totalBeforeAdd = dao.all(session).size();

        ControlStrategy element = newControlStrategy();

        session.clear();
        try {
            List list = dao.all(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    private ControlStrategy newControlStrategy() {
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        dao.add(element, session);
        return element;
    }

public void testShouldUpdateControlStrategyOnUpdate() throws EmfException {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        ControlStrategy element = newControlStrategy();
        session.clear();
        
        EmfDataset dataset = dataset("detailed dataset");
        try {
            ControlStrategy locked = dao.obtainLocked(owner, element, session);
            
            assertEquals(locked.getLockOwner(), owner.getUsername());
            locked.setName("TEST");
            locked.setFilter("WHERE SCC=20210000");
            
            session.clear();
            ControlStrategy modified = dao.update(locked, session);
            
            assertEquals("TEST", locked.getName());
            assertEquals("WHERE SCC=20210000",locked.getFilter());
            assertEquals(modified.getLockOwner(), null);
            
        } finally {
            remove(element);
            remove(dataset);
        }
    }    private EmfDataset dataset(String name) {
        User owner = new UserDAO().get("emf", session);

        EmfDataset dataset = new EmfDataset();
        dataset.setName(name);
        dataset.setCreator(owner.getUsername());

        save(dataset);
        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

    public void testShouldRemoveControlStrategy() {
        int totalBeforeAdd = dao.all(session).size();
        ControlStrategy element = newControlStrategy();
        session.clear();
        int totalAfterAdd = dao.all(session).size();

        assertEquals(totalAfterAdd, totalBeforeAdd + 1);

        try {
            dao.remove(element, session);
            int totalAfterRemove = dao.all(session).size();
            assertEquals(totalBeforeAdd, totalAfterRemove);
        } catch (Exception e) {
            remove(element);
        }
    }

}
