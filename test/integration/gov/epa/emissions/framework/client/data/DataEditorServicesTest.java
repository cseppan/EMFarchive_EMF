package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.services.WebServicesIntegrationTestCase;

public class DataEditorServicesTest extends WebServicesIntegrationTestCase {

    private DataEditorServices services;

    protected void setUp() {
        services = serviceLocator.getDataEditorServices();
    }

    public void testShouldReturnOnePage() throws EmfException {
         Page page = services.getPage("orlnp001",1);
        assertTrue(page != null);
    }


}
