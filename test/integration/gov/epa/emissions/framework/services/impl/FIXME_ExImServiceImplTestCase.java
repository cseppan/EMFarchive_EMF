package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.ExImServiceTestCase;
import gov.epa.emissions.framework.services.UserService;

public abstract class FIXME_ExImServiceImplTestCase extends ExImServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();

        ExImService exim = new ExImServiceImpl(emf(), super.dbServer(), sessionFactory);
        UserService user = new UserServiceImpl(sessionFactory);
        DataCommonsServiceImpl commons = new DataCommonsServiceImpl(sessionFactory);

        super.setUpService(exim, user, commons);
    }

}
