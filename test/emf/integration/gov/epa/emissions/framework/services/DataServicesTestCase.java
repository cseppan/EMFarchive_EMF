package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.configuration.ConfigurationException;

public abstract class DataServicesTestCase extends ServicesTestCase {

    protected DataServices dataService;

    protected DataServicesTestCase(String baseUrlProperty) throws ConfigurationException {
        super(baseUrlProperty);
    }

    protected void setUp() {
        RemoteServiceLocator serviceLocator = new RemoteServiceLocator(baseUrl);
        dataService = serviceLocator.getDataServices();
    }

public void testInsert(){
	int firstCount = 0;
	int secondCount = 0;
	
	try {

	//Count the number of records in the table
	Dataset[] allDsets = dataService.getDatasets();
	firstCount = allDsets.length;
	
	//insert the new record
		EmfDataset aDset = new EmfDataset();
		aDset.setName("NewDataset");
		aDset.setCountry("USA");
		aDset.setCreator("cdcruz");
		aDset.setDatasetType("ORL Waypoint Test");
		aDset.setDescription("A Test of a ficticious dataset");
		aDset.setRegion("MER");
		aDset.setStartDateTime(new Date());
		aDset.setStopDateTime(new Date());
		aDset.setTemporalResolution("TR1");
		aDset.setUnits("metric");
		aDset.setYear(43);
		
    	//Map of datatables
    	HashMap dataTables = new HashMap();
    	dataTables.put("A","B");
    	dataTables.put("C","D");

    	aDset.setDataTables(dataTables);
		dataService.insertDataset(aDset);

		//Count the number of records in the table		
		allDsets = dataService.getDatasets();
		secondCount = allDsets.length;
		
		//If new count is one more than first count then assert true
		assertEquals(1,(secondCount-firstCount));
		
	} catch (EmfException e) {
		e.printStackTrace();
	}
	
	
}


}
