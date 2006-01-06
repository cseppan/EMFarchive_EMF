package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DatasetDaoTest extends ServicesTestCase {

    private DatasetDao dao;

    private Session session;

    private HibernateSessionFactory sessionFactory;

    protected void doSetUp() throws Exception {
        sessionFactory = new HibernateSessionFactory(sessionFactory());
        dao = new DatasetDao();
        session = sessionFactory.getSession();
    }

    protected void doTearDown() throws Exception {// no op
        session.close();
    }

    public void testShouldGetAllSectors() {
        List all = dao.all(session);
        assertEquals(0, all.size());
    }

    public void testShouldGetDatasetById() throws Exception {
        EmfDataset dataset = newDataset();

        try {
            EmfDataset result = dao.get(dataset.getDatasetid(), session);

            assertEquals(dataset.getDatasetid(), result.getDatasetid());
            assertEquals(dataset.getName(), result.getName());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldAddDatasetToDatabaseOnAdd() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset-dao-test");
        dataset.setCreator("creator");

        try {
            dao.add(dataset, session);
            EmfDataset result = dataset(dataset.getName());

            assertEquals(dataset.getDatasetid(), result.getDatasetid());
            assertEquals(dataset.getName(), result.getName());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldUpdateDatasetOnUpdate() throws Exception {
        EmfDataset dataset = newDataset();
        dataset.setCountry("test-country");

        try {
            dao.update(dataset, session);
            EmfDataset result = dataset(dataset.getName());

            assertEquals(dataset.getDatasetid(), result.getDatasetid());
            assertEquals("test-country", result.getCountry());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldRemoveDatasetFromDatabaseOnRemove() throws Exception {
        EmfDataset dataset = newDataset();

        dao.remove(dataset, session);
        EmfDataset result = dataset(dataset.getName());

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

    private void remove(EmfDataset dataset) {
        Transaction tx = session.beginTransaction();
        session.delete(dataset);
        tx.commit();
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

        return dataset(dataset.getName());
    }

    private EmfDataset dataset(String name) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfDataset.class).add(Restrictions.eq("name", name));
            tx.commit();

            return (EmfDataset) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
