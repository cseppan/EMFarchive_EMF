package gov.epa.emissions.framework;

import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.framework.db.LocalHibernateConfiguration;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public abstract class HibernateTestCase extends PersistenceTestCase {

    private SessionFactory sessionFactory = null;

    protected void setUp() throws Exception {
        super.setUp();
        sessionFactory = new LocalHibernateConfiguration().factory();
    }

    protected void doTearDown() throws Exception {
        sessionFactory.close();
    }

    protected Session session() {
        return sessionFactory.openSession();
    }
}
