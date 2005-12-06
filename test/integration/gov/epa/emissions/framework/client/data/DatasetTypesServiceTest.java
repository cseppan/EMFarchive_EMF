package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

public class DatasetTypesServiceTest extends ServicesTestCase {

    private DatasetTypeService services;

    protected void setUp() throws Exception {
        super.setUp();

        ServiceLocator serviceLocator = serviceLocator();
        services = serviceLocator.getDatasetTypesService();
    }

    public void testShouldReturnCompleteListOfDatasetTypes() throws EmfException {
        DatasetType[] types = services.getDatasetTypes();
        assertEquals(8, types.length);
    }

    public void testShouldUpdate() throws Exception {
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
