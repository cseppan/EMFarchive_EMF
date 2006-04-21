package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.ServiceTestCase;

import java.util.List;

public class ControlStrategyDAOTest extends ServiceTestCase {

    private ControlStrategyDAO dao;

    protected void doSetUp() throws Exception {
        dao = new ControlStrategyDAO();
    }

    protected void doTearDown() throws Exception {
        //no op
    }
    
    public void testShouldPersistEmptyControlStrategyOnAdd() {
        int totalBeforeAdd = dao.getControlStrategies(session).size();

        ControlStrategy element = new ControlStrategy("test" + Math.random());
        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getControlStrategies(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }


}
