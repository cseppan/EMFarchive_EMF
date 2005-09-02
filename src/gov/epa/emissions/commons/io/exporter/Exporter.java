package gov.epa.emissions.commons.io.exporter;

import gov.epa.emissions.commons.io.EmfDataset;

import java.io.File;

/**
 * The exporter interface for writing a table type to a text file.
 */
public interface Exporter {
    
    //FIXME: change to an IOException
    //void exportTableToFile(Dataset dataset, String fileName) throws Exception;

	public void run(EmfDataset dataset, File file) throws Exception;
}
