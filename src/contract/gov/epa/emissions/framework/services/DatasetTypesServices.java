package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;

public interface DatasetTypesServices extends EMFServices {

    public DatasetType[] getDatasetTypes() throws EmfException;
    public void insertDatasetType(DatasetType datasetType) throws EmfException;
    public void updateDatasetType(DatasetType datasetType) throws EmfException;
    

}
