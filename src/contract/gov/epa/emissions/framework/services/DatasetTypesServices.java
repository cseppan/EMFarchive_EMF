package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;

public interface DatasetTypesServices extends EMFServices {

    DatasetType[] getDatasetTypes() throws EmfException;
    void insertDatasetType(DatasetType datasetType) throws EmfException;
    void updateDatasetType(DatasetType datasetType) throws EmfException;

}
