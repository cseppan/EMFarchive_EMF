package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.db.PostgresDbUpdate;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;
import gov.epa.emissions.framework.services.impl.StatusServiceImpl;

import java.util.Date;

public abstract class StatusServiceTestCase extends ServicesTestCase {

    private StatusService service;

    private StatusServiceImpl helper;

    protected void setUpService(StatusService service, HibernateSessionFactory sessionFactory) {
        this.service = service;
        helper = new StatusServiceImpl(sessionFactory);
    }

    public void testShouldGetAllStatusMessages() throws Exception {
        Status status = new Status();
        status.setMessage("test message");
        status.setMessageType("type");
        status.setUsername("user");
        status.setTimestamp(new Date());
        
        helper.create(status);

        try {
            Status[] results = service.getAll("user");
            assertEquals(1, results.length);
        } finally {
            PostgresDbUpdate update = new PostgresDbUpdate();
            update.deleteAll("emf.statusmessages");
        }
    }

}
