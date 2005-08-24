package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;

public interface ExImServices extends EMFService {

    public void startImport(User user, String fileName, DatasetType datasetType) throws EmfException;

    public DatasetType[] getDatasetTypes() throws EmfException;

    public void insertDatasetType(DatasetType datasetType) throws EmfException;

}
