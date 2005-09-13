package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;

import java.util.Random;

import org.apache.commons.configuration.ConfigurationException;

public abstract class DatasetTypesServicesTestCase extends ServicesTestCase {

    protected ExImServices eximService;

    protected DatasetTypesServicesTestCase(String baseUrlProperty) throws ConfigurationException {
        super(baseUrlProperty);
    }

    protected void setUp() {
        RemoteServiceLocator serviceLocator = new RemoteServiceLocator(baseUrl);
        eximService = serviceLocator.getExImServices();
    }

    public void testInsert() throws EmfException {
        int firstCount = 0;
        int secondCount = 0;

        // Count the number of records in the table
        DatasetType[] allDsetTypes = eximService.getDatasetTypes();
        firstCount = allDsetTypes.length;

        // insert the new record
        DatasetType aDsetType = new DatasetType();
        Random rand = new Random();
        aDsetType.setName("NewDataset" + rand.nextInt());
        aDsetType.setDescription("Foo Bar= " + rand.nextInt());
        aDsetType.setMaxfiles(rand.nextInt());
        aDsetType.setMaxfiles(rand.nextInt());

        // FIXME: figure a way to update sequence count for the table in
        // Postgres
        
        // eximService.insertDatasetType(aDsetType);
        //
        // // Count the number of records in the table
        // allDsetTypes = eximService.getDatasetTypes();
        // secondCount = allDsetTypes.length;
        //
        // // If new count is one more than first count then assert true
        // assertEquals(1, (secondCount - firstCount));
    }

}
