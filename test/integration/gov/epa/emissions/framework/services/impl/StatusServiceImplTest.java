package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.StatusServiceTestCase;

public class StatusServiceImplTest extends StatusServiceTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        super.setUpService(new StatusServiceImpl(sessionFactory), sessionFactory);
    }

}
