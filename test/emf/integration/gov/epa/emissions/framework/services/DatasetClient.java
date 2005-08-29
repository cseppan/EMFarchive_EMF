package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.impl.DataServicesImpl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
        log.info("IN CONSTRUCTOR");
        svcLoc = new RemoteServiceLocator("http://localhost:8080/emf/services");
        dataSvc = svcLoc.getDataServices();
        
        insertDataset();
        getDatasets();
        log.info("END CONSTRUCTOR");

    }

 
    private void getDatasets() {
    	
        try {
            Dataset[] dataSets = dataSvc.getDatasets();
            if (dataSets == null){
            	System.out.println("NULL DATASETS");
            }else{
                System.out.println("Total number of datasettypes: " + dataSets.length);            	
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    	dataSvc.insertDataset(aDset);
      System.out.println("HibClient: After call to insert Dataset");
	}


	public static void main(String[] args) throws Exception {
        new DatasetClient();
    }
}
