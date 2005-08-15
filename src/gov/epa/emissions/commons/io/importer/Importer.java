package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.io.Dataset;

import java.io.File;

public interface Importer {
    /**
     * This method will put the files into the dataset and database, overwriting
     * existing tables if authorized.
     */
    public void run(File[] files, Dataset dataset, boolean overwrite) throws Exception;

}
