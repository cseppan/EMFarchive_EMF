package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.Dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains all the common features for importing formatted files.
 * FIXME: split the larger methods, and reorganize
 */
public abstract class FormattedImporter implements Importer {
    /** Dataset in which imported data is stored */
    protected Dataset dataset = null;

    protected boolean useTransactions = false;

    protected DbServer dbServer;

    protected TableTypes tableTypes;

    protected FormattedImporter(TableTypes tableTypes, DbServer dbServer) {
        this.tableTypes = tableTypes;
        this.dbServer = dbServer;
    }

    protected final File[] checkFiles(String datasetType, File[] files) throws Exception {
        // FIXME: why is ORL referenced in this base class ?
        TableType tableType = tableTypes.type(datasetType);

        // flags for when we find a file for the table type
        String[] baseTableTypes = tableType.baseTypes();
        boolean[] tableTypeFound = new boolean[baseTableTypes.length];
        // initially flags set to false
        Arrays.fill(tableTypeFound, false);
        // List of files to actually import
        List foundFiles = new ArrayList();

        // if there is only one file, we have the file we want for this type
        if (baseTableTypes.length == 1 && files.length == 1) {
            foundFiles.add(files[0]);
            tableTypeFound[0] = true;
        } else {
            // table types must be sorted in order for binary search to work.
            Arrays.sort(baseTableTypes);
            // Not all File objects in the files array need to be read in.
            // Make sure there is one and only one file for each necessary type.
            // Throw exception if there are multiple files for a necessary type.
            // Ignore (do not import) unnecessary files.
            for (int i = 0; i < files.length; i++) {
                String referenceTableType = ReferenceTable.getTableType(datasetType, files[i].getName());
                // if it is a valid file in the first place (binary search
                // doesn't work for null)
                if (referenceTableType != null) {
                    int searchIndex = Arrays.binarySearch(baseTableTypes, referenceTableType);
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
                            throw new Exception("Multiple files for table type \"" + referenceTableType
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
     * Override this method in order to perform an necessary post processing,
     * meaning any processing to ocur after all of the valid data tuples are
     * inserted into the table. This is table type specific processing performed
     * after each table is imported, as opposed to dataset type specific
     * processing in postImport() which is performed after ALL tables are
     * imported. Default behavior is to not perform any post processing.
     * 
     * @param tableType2
     */
    protected void postProcess(Datasource datasource, String table, String tableType) throws Exception {
        return;/* DO NOTHING */
    }

    protected abstract String[] breakUpLine(String line, int[] widths) throws Exception;

    protected final void setDataSources(File[] files) {
        String datasetType = dataset.getDatasetType();
        // get all the table types for the dataset type
        TableType tableType = tableTypes.type(datasetType);
        Map dataSources = new HashMap();/* <TableType, String> */
        String[] tableTypes = tableType.baseTypes();
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
                String referenceTableType = ReferenceTable.getTableType(datasetType, files[i].getName());
                int searchIndex = Arrays.binarySearch(tableTypes, referenceTableType);
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

    protected void checkForIndexOutOfBounds(int index, String[] stringlets, String line) throws Exception {
        // check for index out of bounds
        if (index >= stringlets.length) {
            throw new Exception("There are more tokens than expected for the following line:\n" + line);
        }
    }
}
