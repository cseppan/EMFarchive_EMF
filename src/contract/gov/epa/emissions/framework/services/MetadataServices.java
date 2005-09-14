package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;

public interface MetadataServices {

    public DatasetType[] getDatasetTypes() throws EmfException;

    //FIXME: Move this to (Meta)DataServices ??
    public void insertDatasetType(DatasetType datasetType) throws EmfException;

}
