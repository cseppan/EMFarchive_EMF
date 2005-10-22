/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: DataServices.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public interface DataServices {

	// Datasets
	EmfDataset[] getDatasets() throws EmfException;
	EmfDataset[] getDatasets(User user) throws EmfException;
	void insertDataset(EmfDataset aDataset) throws EmfException;
	void updateDataset(EmfDataset aDset) throws EmfException;
	
	//Countries
	Country[] getCountries() throws EmfException;
	void addCountry(Country country) throws EmfException;
	void updateCountry(Country country) throws EmfException;

	//Sectors
	Sector[] getSectors() throws EmfException;
	void addSector(Sector sector) throws EmfException;
	void updateSector(Sector sector) throws EmfException;
	
    //Dataset Types
    DatasetType[] getDatasetTypes() throws EmfException;
    void insertDatasetType(DatasetType datasetType) throws EmfException;
    void updateDatasetType(DatasetType datasetType) throws EmfException;
}