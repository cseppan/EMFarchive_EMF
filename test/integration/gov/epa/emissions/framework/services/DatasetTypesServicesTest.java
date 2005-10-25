package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.db.ExImDbUpdate;

import java.util.Random;

public class DatasetTypesServicesTest extends ServicesTestCase {

    protected DatasetTypesServices services;

    private EmfDataset dataset;

    protected void setUp() {
        services = serviceLocator.getDatasetTypesServices();

        dataset = new EmfDataset();
        Random random = new Random();
        dataset.setName("ORL NonPoint - ExImServicesTest" + random.nextInt());
        dataset.setCreator("creator");
    }

    protected void tearDown() throws Exception {
        ExImDbUpdate dbUpdate = new ExImDbUpdate();
        dbUpdate.deleteAllDatasets();
    }

    public void testFetchDatasetTypesReturnsFourORLTypes() throws EmfException {
        DatasetType[] datasetTypes = services.getDatasetTypes();
        assertTrue("Should have atleast 4 ORL types", datasetTypes.length >= 4);
    }

}
