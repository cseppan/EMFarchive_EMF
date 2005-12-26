package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataCommonsServiceTestCase;

public class DataCommonsServiceImplTest extends DataCommonsServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        super.setUpService(new DataCommonsServiceImpl(sessionFactory));
    }

    
}
