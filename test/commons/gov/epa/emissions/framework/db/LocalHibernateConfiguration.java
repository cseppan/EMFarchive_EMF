package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.Config;

import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class LocalHibernateConfiguration {

    private SessionFactory sessionFactory;

    public LocalHibernateConfiguration() throws Exception {
        Configuration config = new Configuration().configure();
        Properties props = config.getProperties();
        props.remove("hibernate.connection.datasource");

        props.putAll(testsConfig());

        config = config.setProperties(props);
        sessionFactory = config.buildSessionFactory();
    }

    private Map testsConfig() throws Exception {
        return new Config("test/postgres.conf").properties();
    }

    public SessionFactory factory() {
        return sessionFactory;
    }
}
