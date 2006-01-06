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
