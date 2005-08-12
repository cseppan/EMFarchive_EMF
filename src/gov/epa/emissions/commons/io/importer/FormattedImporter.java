package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.DataAcceptor;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.Dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains all the common features for importing formatted files.
 * FIXME: split the larger methods, and reorganize
 */
public abstract class FormattedImporter implements Importer {
    /** Dataset in which imported data is stored */
    protected Dataset dataset = null;

    protected boolean useTransactions = false;

    protected DbServer dbServer;

    protected FormattedImporter(DbServer dbServer) {
        this.dbServer = dbServer;
    }

    public final String importFile(File file, Datasource datasource, BufferedReader reader, String[] columnNames,
            String[] columnTypes, int[] columnWidths, boolean overwrite) throws Exception {
        // get the name of the file
        String fileName = file.getName();

        String datasetType = dataset.getDatasetType();
        // get the table type
        String tableType = null;
        String[] tableTypes = DatasetTypes.getTableTypes(datasetType);
        // remove summary table if more than one TableType is possible
        if (tableTypes.length > 1) {
            tableTypes = removeSummaryTable(datasetType, tableTypes);
            // use fileName if more than one TableType is still possible
            if (tableTypes.length > 1) {
                tableType = (String) TableTypes.getTableType(dataset.getDatasetType(), fileName);
            } else {
                tableType = tableTypes[0];
            }
        } else {
            tableType = tableTypes[0];
        }
        if (tableType == null) {
            throw new Exception("Could not determine table type for file name: " + fileName);
        }

        // use the table type to get the table name
        String tableName = ((String) dataset.getDataTable(tableType)).trim();
        String qualifiedTableName = datasource.getName() + "." + tableName;

        if (tableName == null) {
            throw new Exception("The dataset did not specify the table name for file name: " + fileName);
        } else if (tableName.length() == 0) {
            throw new Exception("The table name must be at least one character long for file name: " + fileName);
        }

        // instantiate an database acceptor.. the acceptor takes care of all
        // database operations.. set the database name and table name to the
        // acceptor so it knows where to put the data in.
        DataAcceptor acceptor = datasource.getDataAcceptor();
        // delete table if overwrite
        if (overwrite) {
            acceptor.deleteTable(qualifiedTableName);
        }
        // else make sure table does not exist
        else if (acceptor.tableExists(qualifiedTableName)) {
            throw new Exception("The table \"" + qualifiedTableName
                    + "\" already exists. Please select 'overwrite tables if exist' or choose a new table name.");
        }
        acceptor.setTable(qualifiedTableName);

        acceptor.startAcceptingData();
        acceptor.createTable(columnNames, columnTypes, null, false);
        String line = null;
        String[] data = null;
        int numRows = 0;

        // kick out invalid data lines
        int kickOutRows = 0;
        PrintWriter writer = null;
        String canonicalFileName = file.getCanonicalPath();
        int txtIndex = canonicalFileName.indexOf(".txt");
        String writerFileName = "";
        File writerFile = null;
        // find unique file name
        for (int i = 0; writerFile == null || writerFile.exists(); i++) {
            writerFileName = canonicalFileName.substring(0, txtIndex) + ".reimport." + i
                    + canonicalFileName.substring(txtIndex);
            writerFile = new File(writerFileName);
        }

        // read lines in one at a time and put the data into database.. this
        // will avoid huge memory consumption
        while ((line = reader.readLine()) != null) {
            // skip over non data lines as needed
            if (!line.startsWith("#") && line.trim().length() > 0) {
                data = breakUpLine(line, columnWidths);
                if (validData(data, tableType)) {
                    // data = supplementData(data, tableType);
                    acceptor.insertRow(data, columnTypes);
                    numRows++;
                } else {
                    // create new file writer
                    if (writer == null) {
                        writer = new PrintWriter(new BufferedWriter(new FileWriter(writerFileName)));
                        writeKickOutHeaders(writer);
                    }
                    writer.println(line);
                    kickOutRows++;
                }
            }
        }// while file is not empty

        // perform capable table type specific processing
        postProcess(acceptor, tableType);

        // when all the data is done ingesting..
        // close the database connections by calling acceptor.finish..
        // and close the reader & writer as well..
        acceptor.finishAcceptingData();
        reader.close();
        if (writer != null)
            writer.close();

        if (kickOutRows > 0)
            System.out.println("Kicked out " + kickOutRows + " rows to file " + writerFileName);

        return tableName;
    }

    protected void writeKickOutHeaders(PrintWriter writer) {
        /* DO NOTHING */
    }// writeKickOutHeaders(PrintWriter)

    protected final File[] checkFiles(String datasetType, File[] files) throws Exception {
        // get all the table types for the dataset type
        String[] tableTypes = DatasetTypes.getTableTypes(datasetType);
        // remove summary table from list (ok for NIF, not needed for IDA)
        if (tableTypes.length != 1) {
            tableTypes = removeSummaryTable(datasetType, tableTypes);
        }

        // flags for when we find a file for the table type
        boolean[] tableTypeFound = new boolean[tableTypes.length];
        // initially flags set to false
        Arrays.fill(tableTypeFound, false);
        // List of files to actually import
        List foundFiles = new ArrayList();

        // if there is only one file, we have the file we want for this type
        if (tableTypes.length == 1 && files.length == 1) {
            foundFiles.add(files[0]);
            tableTypeFound[0] = true;
        } else {
            // table types must be sorted in order for binary search to work.
            Arrays.sort(tableTypes);
            // Not all File objects in the files array need to be read in.
            // Make sure there is one and only one file for each necessary type.
            // Throw exception if there are multiple files for a necessary type.
            // Ignore (do not import) unnecessary files.
            for (int i = 0; i < files.length; i++) {
                String tableType = TableTypes.getTableType(datasetType, files[i].getName());
                // if it is a valid file in the first place (binary search
                // doesn't work for null)
                if (tableType != null) {
                    int searchIndex = Arrays.binarySearch(tableTypes, tableType);
                    // Valid table type. Check for duplicate file names for
                    // table type
                    // and make sure file name ends with ".txt".
                    if (searchIndex >= 0 && files[i].getName().toLowerCase().endsWith(".txt")) {
                        // if no file yet for this table type
                        if (!tableTypeFound[searchIndex]) {
                            // flag that we found a file
                            tableTypeFound[searchIndex] = true;
                            // add to list of files to actually import
                            foundFiles.add(files[i]);
                        }
                        // else
                        else {
                            // already have a file for this table type
                            throw new Exception("Multiple files for table type \"" + tableType
                                    + "\" are not allowed in the same directory");
                        }
                    }
                }
            }
        }

        // check that a file was found for all table types
        boolean[] tableTypeChecker = new boolean[tableTypeFound.length];
        Arrays.fill(tableTypeChecker, true);
        if (!Arrays.equals(tableTypeFound, tableTypeChecker)) {
            // missing a file for or more table types
            throw new Exception("Missing a file for one or more table types for dataset type \"" + datasetType + "\"");
        }

        return (File[]) foundFiles.toArray(new File[0]);
    }

    /**
     * Override this method to perform any in-line analysis on data, meaning any
     * checks on data which would designate that it should be excluded from
     * insertion into the current table and subsequently kicked out (written) to
     * an 'extra' data file containing other kicked out data. Default behavior
     * is to assume all data lines are valid.
     * 
     * @param data -
     *            the data read from the input file
     * @param tableType -
     *            the type of the table
     * @return true if the data is valid for the table type
     * @throws Exception
     */
    protected boolean validData(String[] data, String tableType) throws Exception {
        /* DO NOTHING */
        return true;
    }

    /**
     * Override this method in order to perform an necessary post processing,
     * meaning any processing to ocur after all of the valid data tuples are
     * inserted into the table. This is table type specific processing performed
     * after each table is imported, as opposed to dataset type specific
     * processing in postImport() which is performed after ALL tables are
     * imported. Default behavior is to not perform any post processing.
     */
    protected void postProcess(DataAcceptor acceptor, String tableType) throws Exception {
        /* DO NOTHING */
        return;
    }

    protected abstract String[] breakUpLine(String line, int[] widths) throws Exception;

    /**
     * @pre files != null, dataset != null
     */
    protected final void setDataSources(File[] files) {
        String datasetType = dataset.getDatasetType();
        // get all the table types for the dataset type
        String[] tableTypes = DatasetTypes.getTableTypes(datasetType);
        // remove summary table from list (ok for NIF, not needed for IDA)
        if (tableTypes.length != 1) {
            tableTypes = removeSummaryTable(datasetType, tableTypes);
        }

        java.util.Map/* <TableType, String> */dataSources = new java.util.HashMap/*
                                                                                     * <TableType,
                                                                                     * String>
                                                                                     */();
        String[] absolutePaths = new String[tableTypes.length];
        // if there is only one file, we have the file we want for this type
        if (tableTypes.length == 1 && files.length == 1) {
            absolutePaths = new String[] { files[0].getAbsolutePath() };
        }
        // else, search through the files to match the table type with its file
        else {
            // table types must be sorted in order for binary search to work.
            Arrays.sort(tableTypes);
            // initially absolute paths set to null
            Arrays.fill(absolutePaths, null);
            // for(File file : files)
            for (int i = 0; i < files.length; i++) {
                String tableType = TableTypes.getTableType(datasetType, files[i].getName());
                int searchIndex = Arrays.binarySearch(tableTypes, tableType);
                absolutePaths[searchIndex] = files[i].getAbsolutePath();
            }
        }

        // map data sources from table type to absolute path
        for (int i = 0; i < tableTypes.length; i++) {
            dataSources.put(tableTypes[i], absolutePaths[i]);
        }
        dataset.setDataSources(dataSources);
    }// setDataSources(File[])

    public final Dataset getDataset() {
        return dataset;
    }

    protected String[] removeSummaryTable(String datasetType, String[] tableTypes) {
        String summaryTable = DatasetTypes.getSummaryTableType(datasetType);
        // must send returned array to concrete List constructor in order to use
        // remove method
        List tableTypeList = new ArrayList(Arrays.asList(tableTypes));
        tableTypeList.remove(summaryTable);
        return (String[]) tableTypeList.toArray(new String[0]);
    }

    protected void checkForIndexOutOfBounds(int index, String[] stringlets, String line) throws Exception {
        // check for index out of bounds
        if (index >= stringlets.length) {
            throw new Exception("There are more tokens than expected for the following line:\n" + line);
        }
    }
}
