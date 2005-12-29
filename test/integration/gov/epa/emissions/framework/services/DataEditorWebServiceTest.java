package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.ServiceLocator;

public class DataEditorWebServiceTest extends DataEditorServiceTestCase {

    protected void doSetUp() throws Exception {
        ServiceLocator serviceLocator = serviceLocator();
        super.setUpService(serviceLocator.dataEditorService());
    }

}
