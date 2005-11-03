package gov.epa.emissions.framework;

import gov.epa.emissions.commons.Config;

import java.util.Map;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import junit.framework.TestCase;

public abstract class HibernateTestCase extends TestCase {

    private SessionFactory sessionFactory = null;

    protected Session session;

    protected void setUp() throws Exception {
        sessionFactory = config().buildSessionFactory();
        session = sessionFactory.openSession();
    }

    private Configuration config() throws Exception {
        Configuration config = new Configuration().configure();
        Properties props = config.getProperties();
        props.remove("hibernate.connection.datasource");

        props.putAll(testsConfig());

        config = config.setProperties(props);

        return config;
    }

    private Map testsConfig() throws Exception {
        return new Config("test/test.conf").properties();
    }

    protected void tearDown() {
        session.close();
    }

}
