package gov.epa.emissions.framework;

import gov.epa.emissions.framework.db.LocalHibernateConfiguration;
import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public abstract class HibernateTestCase extends TestCase {

    private SessionFactory sessionFactory = null;

    protected void setUp() throws Exception {
        sessionFactory = new LocalHibernateConfiguration().factory();
    }

    protected void tearDown() {
        sessionFactory.close();
    }

    protected Session session() {
        return sessionFactory.openSession();
    }
}
