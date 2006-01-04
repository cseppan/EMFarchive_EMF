package gov.epa.emissions.framework;

import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.framework.db.LocalHibernateConfiguration;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public abstract class HibernateTestCase extends PersistenceTestCase {

    private HibernateSessionFactory sessionFactory;

    protected Session session;

    protected void setUp() throws Exception {
        super.setUp();
        sessionFactory = new HibernateSessionFactory(sessionFactory());
        session = sessionFactory.getSession();
    }

    protected void doTearDown() throws Exception {
        session.close();
    }

    private SessionFactory sessionFactory() throws Exception {
        LocalHibernateConfiguration config = new LocalHibernateConfiguration();
        return config.factory();
    }
}
