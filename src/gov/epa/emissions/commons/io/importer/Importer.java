package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.Datasource;

import java.io.BufferedReader;
import java.io.File;

public interface Importer {
    /* read ahead limit ~ 100 MB */
    public static final long READ_AHEAD_LIMIT = 105000000L;

    /**
     * This method will put the files into the dataset and database, overwriting
     * existing tables if authorized.
     * 
     * @param files -
     *            the files to put into the dataset and database
     * @param overwrite -
     *            whether or not to overwrite existing tables
     * @param dataset -
     *            the dataset to import to data into
     * @throws Exception
     */
    public void putIntoDatabase(File[] files, boolean overwrite, Dataset dataset) throws Exception;

    /**
     * this method will take in a file as an argument and ingest if into the
     * database
     * 
     * @param file
     *            the file to be ingested in
     * @param dbName
     *            the database name
     * @return String the tableName
     */
    public String importFile(File file, Datasource datasource, BufferedReader reader, String[] columnNames,
            String[] columnTypes, int[] columnWidths, boolean overwrite) throws Exception;
}
