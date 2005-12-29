package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataCommonsServiceTestCase;
import gov.epa.emissions.framework.services.UserService;

public abstract class FIXME_DataCommonsServiceImplTestCase extends DataCommonsServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());

        DataCommonsServiceImpl commonsService = new DataCommonsServiceImpl(sessionFactory);
        UserService userService = new UserServiceImpl(emf(), super.dbServer());

        super.setUpService(commonsService, userService);
    }

}
