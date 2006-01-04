package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.ExImServiceTestCase;
import gov.epa.emissions.framework.services.UserService;

public class ExImServiceImplTest extends ExImServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());

        ExImService exim = new ExImServiceImpl(emf(), super.dbServer(), sessionFactory);
        UserService user = new UserServiceImpl(sessionFactory);
        DataCommonsServiceImpl commons = new DataCommonsServiceImpl(sessionFactory);

        super.setUpService(exim, user, commons);
    }

}
