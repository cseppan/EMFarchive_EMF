package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataEditorServiceTestCase;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

public class DataEditorWebServiceTest extends DataEditorServiceTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        
        ServiceLocator serviceLocator = serviceLocator();
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        super.setUpService(serviceLocator.dataEditorService(), sessionFactory);
    }

}
