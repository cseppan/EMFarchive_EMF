/*
 * Creation on Sep 14, 2005
 * Eclipse Project Name: EMF
 * File Name: TestBaseFolders.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;

public class TestDatasets {
	private String url="http://localhost:8080/emf/services/gov.epa.emf.services.DataServices";

	
	public TestDatasets() throws EmfException {
		super();
		//getDatasets();
		EmfDataset aDset = getDataset();
		System.out.println("READY TO UPDATE: " + aDset.getName());
		aDset.setName("Busstop");
		updateDataset(aDset);
	}

	
	private void updateDataset(EmfDataset dset) throws EmfException {
	  DataServicesTransport dtx = new DataServicesTransport(url);
	  dtx.updateDataset(dset);	
	}


	private EmfDataset getDataset() throws EmfException {
		EmfDataset aDset=null;
		DataServicesTransport dtx = new DataServicesTransport(url);
		EmfDataset[] allDatasets = dtx.getDatasets();
		
		System.out.println(" Number of entries " + allDatasets.length);
		for (int i=0; i<allDatasets.length;i++){
			if (allDatasets[i].getDatasetid()==1){
				aDset = allDatasets[i];
			}
		}
		return aDset;
	}


	private void getDatasets() throws EmfException{
		DataServicesTransport dtx = new DataServicesTransport(url);
		Dataset[] allDatasets = dtx.getDatasets();
		
		System.out.println(" Number of entries " + allDatasets.length);
		for (int i=0; i<allDatasets.length;i++){
			System.out.println(allDatasets[i].getName());
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new TestDatasets();
		} catch (EmfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
