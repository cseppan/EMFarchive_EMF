package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.ServiceLocator;

public class DataEditorWebServiceTest extends DataEditorService_DataTestCase {

    protected void doSetUp() throws Exception {
        ServiceLocator locator = serviceLocator();
        super.setUpService(locator.dataEditorService());
    }

}
