package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.ExImServiceTestCase;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.services.impl.DatasetTypeServiceImpl;
import gov.epa.emissions.framework.services.impl.ExImServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.UserServiceImpl;

public class ExImServiceImplTest extends ExImServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());

        ExImService exim = new ExImServiceImpl(emf(), super.dbServer(), sessionFactory);
        UserService user = new UserServiceImpl(emf(), super.dbServer());
        DatasetTypeService datasetType = new DatasetTypeServiceImpl(sessionFactory);

        super.setUpService(exim, user, datasetType);
    }

}
