package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.impl.DataServicesImpl;

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.mapping.Map;

/*
 * Created on Aug 1, 2005
 *
 * Eclipse Project Name: Hib
 * Package: 
 * File Name: HibClient.java
 * Author: Conrad F. D'Cruz
 */

/**
 * @author Conrad F. D'Cruz
 *
 */
/**
 * @author Conrad F. D'Cruz
 *
 */
public class DatasetClient {
//http://localhost:8080/emf/services/gov.epa.emf.services.DataServices
	
    private static Log log = LogFactory.getLog(DatasetClient.class);
    private RemoteServiceLocator svcLoc = null;
    private DataServices dataSvc = null;
    /**
     * @throws Exception 
     * 
     */
    public DatasetClient() throws Exception {
        super();
        log.debug("IN CONSTRUCTOR");
        svcLoc = new RemoteServiceLocator("http://localhost:8080/emf/services");
        dataSvc = svcLoc.getDataServices();
        
        insertDataset();
        getDatasets();
        log.debug("END CONSTRUCTOR");

    }

 
    private void getDatasets() {
    	
        try {
            Dataset[] dataSets = dataSvc.getDatasets();
            if (dataSets == null){
            	log.info("NULL DATASETS");
            }else{
                log.info("Total number of datasettypes: " + dataSets.length);            	
            }

        } catch (Exception e) {
            log.error("Error getting datasets",e);
        }
        
	}


	private void insertDataset() throws Exception {
    	 	
    	EmfDataset aDset = new EmfDataset();
    	aDset.setName("ConradDataset");
    	aDset.setCountry("India");
    	aDset.setCreator("cdcruz");
    	aDset.setDatasetType("ORL Stuff");
    	aDset.setDescription("Stuff happens");
    	aDset.setRegion("Mumbai");
    	aDset.setStartDateTime(new Date());
    	aDset.setStopDateTime(new Date());
    	aDset.setTemporalResolution("FooBar");
    	aDset.setUnits("Unity");
    	aDset.setYear(43);
    	//Map of datatables
    	HashMap dataTables = new HashMap();
    	dataTables.put("A","B");
    	dataTables.put("C","D");
    	
    	//Insert the dataset into the schema
    	dataSvc.insertDataset(aDset);
      log.debug("HibClient: After call to insert Dataset");
	}


	public static void main(String[] args) throws Exception {
        new DatasetClient();
    }
}
