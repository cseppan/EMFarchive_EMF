package gov.epa.emissions.commons.io.exporter;

import gov.epa.emissions.commons.io.Dataset;

/**
 * The exporter interface for writing a table type to a text file.
 */
public interface Exporter {
    
    //FIXME: change to an IOException
    void exportTableToFile(String tableType, Dataset dataset, String fileName) throws Exception;
}
