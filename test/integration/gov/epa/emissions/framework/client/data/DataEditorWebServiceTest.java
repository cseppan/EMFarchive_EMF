package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataEditorServiceTestCase;

public class DataEditorWebServiceTest extends DataEditorServiceTestCase {

    protected void doSetUp() throws Exception {
        ServiceLocator serviceLocator = serviceLocator();
        super.setUpService(serviceLocator.dataEditorService());
    }

}
