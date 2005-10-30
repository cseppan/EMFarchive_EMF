package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.WebServicesIntegrationTestCase;

public class DatasetTypesServicesTest extends WebServicesIntegrationTestCase {

    private DatasetTypesServices services;

    protected void setUp() {
        services = serviceLocator.getDatasetTypesServices();
    }

    public void testShouldReturnCompleteListOfDatasetTypes() throws EmfException {
        DatasetType[] types = services.getDatasetTypes();
        assertEquals(8, types.length);
    }

    public void testShouldUpdate() throws EmfException {
        DatasetType[] types = services.getDatasetTypes();
        DatasetType type = types[0];
        String name = type.getName();
        type.setName("TEST");
        services.updateDatasetType(type);

        DatasetType modified = types(type.getDatasettypeid());
        assertEquals("TEST", modified.getName());

        // restore
        modified.setName(name);
        services.updateDatasetType(modified);
    }

    private DatasetType types(long id) throws EmfException {
        DatasetType[] types = services.getDatasetTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getDatasettypeid() == id)
                return types[i];
        }

        return null;
    }

}
