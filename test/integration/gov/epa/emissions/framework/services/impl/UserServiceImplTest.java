package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.services.UserServiceTestCase;

public class UserServiceImplTest extends UserServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());

        UserService userService = new UserServiceImpl(sessionFactory);

        super.setUpService(userService);
    }

}
