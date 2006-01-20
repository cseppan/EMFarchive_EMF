package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataServiceTestCase;
import gov.epa.emissions.framework.services.UserService;

public class DataServiceImplTest extends DataServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();
        DataServiceImpl commonsService = new DataServiceImpl(sessionFactory);
        UserService userService = new UserServiceImpl(sessionFactory);

        super.setUpService(commonsService, userService);
    }

}
