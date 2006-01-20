package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.services.impl.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.impl.ExImServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.UserServiceImpl;

public abstract class FIXME_ExImServiceImplTestCase extends ExImServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();

        ExImService exim = new ExImServiceImpl(emf(), super.dbServer(), sessionFactory);
        UserService user = new UserServiceImpl(sessionFactory);
        DataCommonsServiceImpl commons = new DataCommonsServiceImpl(sessionFactory);

        super.setUpService(exim, user, commons);
    }

}
