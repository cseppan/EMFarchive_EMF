package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class ControlStrategyServiceTest extends ServiceTestCase {

    private ControlStrategyService service;

    private UserServiceImpl userService;

    private HibernateSessionFactory sessionFactory;

    protected void doSetUp() throws Exception {
        sessionFactory = sessionFactory(configFile());
        service = new ControlStrategyServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetControlStrategies() throws Exception {
        int totalBeforeAdd = service.getControlStrategies().length;
        ControlStrategy element = controlStrategy();

        try {
            List list = Arrays.asList(service.getControlStrategies());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    private ControlStrategy controlStrategy() {
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        add(element);
        return element;
    }

    public void testShouldAddControlStrategy() throws Exception {
        int totalBeforeAdd = service.getControlStrategies().length;
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        service.addControlStrategy(element);

        try {
            List list = Arrays.asList(service.getControlStrategies());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldObtainLockedControlStrategy() throws EmfException {
        User owner = userService.getUser("emf");
        ControlStrategy element = controlStrategy();

        try {
            ControlStrategy locked = service.obtainLocked(owner, element);
            assertTrue("Should be locked by owner", locked.isLocked(owner));

            ControlStrategy loadedFromDb = load(element);// object returned directly from the table
            assertEquals(locked.getLockOwner(), loadedFromDb.getLockOwner());
        } finally {
            remove(element);
        }
    }

    public void testShouldReleaseLockedOnControlStrategy() throws EmfException {
        User owner = userService.getUser("emf");
        ControlStrategy element = controlStrategy();

        try {
            ControlStrategy locked = service.obtainLocked(owner, element);
            ControlStrategy released = service.releaseLocked(locked);
            assertFalse("Should have released lock", released.isLocked());

            ControlStrategy loadedFromDb = load(element);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(element);
        }
    }

    public void testShouldUpdateControlStrategy() throws Exception {
        User owner = userService.getUser("emf");
        ControlStrategy element = controlStrategy();
        ControlStrategy released = null;
        try {
            ControlStrategy locked = service.obtainLocked(owner, element);

            session.clear();
            locked.setName("TEST");
            locked.setDescription("TEST control strategy");

            released = service.updateControlStrategy(locked);
            assertEquals("TEST", released.getName());
            assertEquals("TEST control strategy", released.getDescription());
            assertEquals(released.getLockOwner(), null);
            assertFalse("Lock should be released on update", released.isLocked());
        } finally {
            remove(element);
        }
    }

    // Need to populate the test database table emf.strategy_type to pass this test.
    public void testShouldGetStrategyTypes() throws Exception {
        StrategyType[] types = service.getStrategyTypes();
        session.clear();

        assertEquals(2, types.length);
        assertEquals("Max Emissions Reduction", types[1].getName());
        assertEquals("Least Cost", types[0].getName());
    }

    public void testShouldUpdateControlStrategyWithLock() throws Exception {
        User owner = userService.getUser("emf");
        ControlStrategy element = controlStrategy();
        ControlStrategy csWithLock = null;
        try {
            ControlStrategy locked = service.obtainLocked(owner, element);

            session.clear();
            locked.setName("TEST");
            locked.setDescription("TEST control strategy");

            csWithLock = service.updateControlStrategyWithLock(locked);
            assertEquals("TEST", csWithLock.getName());
            assertEquals("TEST control strategy", csWithLock.getDescription());
            assertEquals(csWithLock.getLockOwner(), owner.getUsername());
            assertTrue("Lock should be kept on update", csWithLock.isLocked());
        } finally {
            remove(element);
        }
    }

    public void testShouldRunControlStrategyWithoutError() throws Exception {
        User owner = userService.getUser("emf");
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        StrategyType type = new StrategyType("max red emissions");
        type.setStrategyClassName("gov.epa.emissions.framework.services.cost.analysis.maxreduction.DummyMaxEmsRedStrategy");
        element.setStrategyType(type);

        service.runStrategy(owner, element);
    }

    private ControlStrategy load(ControlStrategy controlStrategy) {
        Transaction tx = null;
        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(ControlStrategy.class).add(
                    Restrictions.eq("name", controlStrategy.getName()));
            tx.commit();

            return (ControlStrategy) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
