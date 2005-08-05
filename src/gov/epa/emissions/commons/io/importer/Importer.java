package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.io.Dataset;

import java.io.BufferedReader;
import java.io.File;

/**
 * @author		Craig Mattocks
 * @version $Id: Importer.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 *
 */
public interface Importer
{
    /**
     * This method will put the files into the dataset and database,
     * overwriting existing tables if authorized.
     * @param files - the files to put into the dataset and database
     * @param overwrite - whether or not to overwrite existing tables
     * @param dataset - the dataset to import to data into
     * @throws Exception
     */
    public void putIntoDatabase(File[] files, boolean overwrite, Dataset dataset) throws Exception;

    /**
	 * this method will take in a file as an argument and ingest if into the
	 * database
	 * @param file the file to be ingested in
	 * @param dbName the database name
	 * @return String the tableName
	 */
	public String importFile(File file, String dbName, BufferedReader reader,
			String[] columnNames, String[] columnTypes, int[] columnWidths,
            boolean overwrite) throws Exception;
}
