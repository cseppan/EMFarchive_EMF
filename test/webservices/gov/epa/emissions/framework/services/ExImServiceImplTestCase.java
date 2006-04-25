package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.exim.ExImService;
import gov.epa.emissions.framework.services.exim.ExImServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class ExImServiceImplTestCase extends ExImServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());

        ExImService exim = new ExImServiceImpl(emf(), super.dbServer(), sessionFactory);
        UserService user = new UserServiceImpl(sessionFactory);
        DataCommonsServiceImpl commons = new DataCommonsServiceImpl(sessionFactory);

        super.setUpService(exim, user, commons);
    }

}
