package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataCommonsServiceTestCase;
import gov.epa.emissions.framework.services.UserService;

public class DataCommonsServiceImplTest extends DataCommonsServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        
        DataCommonsServiceImpl commonsService = new DataCommonsServiceImpl(sessionFactory);
        UserService userService = new UserServiceImpl(sessionFactory);

        super.setUpService(commonsService, userService);
    }

}
