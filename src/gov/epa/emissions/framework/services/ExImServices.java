/*
 * Created on Jul 28, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.commons;
 * File Name: EMFData.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

/**
 * @author Conrad F. D'Cruz
 *
 */
public interface ExImServices extends EMFService{

    public void startImport(String userName, String fileName, String fileType) throws EmfException;
    public void startImport(User user, String fileName, DatasetType datasetType) throws EmfException;
    public DatasetType[] getDatasetTypes() throws EmfException;
    public void insertDatasetType(DatasetType aDstn) throws EmfException;

}
