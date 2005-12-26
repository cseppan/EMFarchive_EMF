package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.StatusServiceTestCase;

public class StatusServiceImplTest extends StatusServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        super.setUpService(new StatusServiceImpl(sessionFactory), sessionFactory);
    }

}
