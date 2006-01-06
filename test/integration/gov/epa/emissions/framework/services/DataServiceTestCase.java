package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public abstract class DataServiceTestCase extends ServicesTestCase {

    private DataService service;

    private HibernateSessionFactory sessionFactory;

    private Session session;

    private UserService userService;

    protected void setUpService(DataService service, UserService userService) throws Exception {
        this.service = service;
        this.userService = userService;

        sessionFactory = new HibernateSessionFactory(sessionFactory());
        session = sessionFactory.getSession();
    }

    protected void doTearDown() throws Exception {// no op
        session.close();
    }

    public void testShouldGetAllDatasets() throws EmfException {
        EmfDataset[] datasets = service.getDatasets();
        assertEquals(0, datasets.length);

        EmfDataset dataset = newDataset();
        try {
            EmfDataset[] postInsert = service.getDatasets();
            assertEquals(1, postInsert.length);
        } finally {
            remove(dataset);
        }
    }

    public void testShouldObtainLockedDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            assertTrue("Should be locked by owner", locked.isLocked(owner));

            EmfDataset loadedFromDb = load(dataset);// object returned directly from the table
            assertEquals(locked.getLockOwner(), loadedFromDb.getLockOwner());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldReleaseLockedDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            EmfDataset released = service.releaseLockedDataset(locked);
            assertFalse("Should have released lock", released.isLocked());

            EmfDataset loadedFromDb = load(dataset);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(dataset);
        }
    }

    private EmfDataset newDataset() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset-dao-test");
        dataset.setCreator("creator");

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
        session.clear();// flush cached objects

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

    private void remove(EmfDataset dataset) {
        session.clear();// flush cached objects

        Transaction tx = session.beginTransaction();
        session.delete(dataset);
        tx.commit();
    }

}
