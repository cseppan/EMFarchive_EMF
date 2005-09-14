/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: DataServices.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.EmfException;

/**
 * @author Conrad F. D'Cruz
 *
 */
public interface DataServices {

	public Dataset[] getDatasets() throws EmfException;
	public Dataset[] getDatasets(User user) throws EmfException;
	public void insertDataset(EmfDataset aDataset) throws EmfException;
}