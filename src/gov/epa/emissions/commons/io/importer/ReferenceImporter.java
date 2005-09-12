package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.Table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintWriter;

/**
 * This class represents the ReferenceImporter for the reference database.
 * TODO: replace by injection. Combine Reference Tables & Reference Importer
 */
public class ReferenceImporter extends FixedFormatImporter {
    private File fieldDefsFile;

    private File referenceFilesDir;

    /** the field definitions file reader * */
    private FieldDefinitionsFileReader fieldDefsReader = null;

    private static final String REF_DIR_NAME = "refFiles";

    public ReferenceImporter(DbServer dbServer, File fieldDefsFileName, File referenceFilesDir, boolean useTransactions) {
        super(new ReferenceTableTypes(), dbServer);
        this.fieldDefsFile = fieldDefsFileName;
        this.referenceFilesDir = referenceFilesDir;
        this.useTransactions = useTransactions;
    }

    /**
     * Take a array of Files and put them database, overwriting existing
     * corresponding tables specified in dataset based on overwrite flag.
     */
    public void run(File[] files, Dataset dataset, boolean overwrite) throws Exception {
        this.dataset = dataset;

        Datasource datasource = dbServer.getReferenceDatasource();
        String type = dataset.getDatasetType();

        files = checkFiles(type, files);

        if (!type.equals(DatasetTypes.REFERENCE)) {
            throw new Exception("Unknown/unhandled reference type: " + type);
        }

        // set the data source for the dataset
        setDataSources(files);

        fieldDefsReader = new FieldDefinitionsFileReader(fieldDefsFile, dbServer.getTypeMapper());

        // import each file (--> database table) one by one..
        for (int i = 0; i < files.length; i++) {
            importFile(files[i], datasource, getDetails(files[i]), overwrite);
        }
    }

    /**
     * import a single file into the specified database
     */
    public void importFile(File file, Datasource datasource, FileColumnsMetadata details, boolean overwrite)
            throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String[] columnTypes = details.getColumnTypes();
        String fileName = file.getName();
        String datasetType = dataset.getDatasetType();
        String tableType = ReferenceTable.getTableType(datasetType, fileName);
        if (tableType == null) {
            throw new Exception("Could not determine table type for file name: " + fileName);
        }
        
        // use the table type to get the table name
        Table table = dataset.getTable(tableType);
        String tableName = table.getTableName().trim();
        String qualifiedTableName = datasource.getName() + "." + tableName;
        
        if (tableName == null) {
            throw new Exception("The dataset did not specify the table name for file name: " + fileName);
        } else if (tableName.length() == 0) {
            throw new Exception("The table name must be at least one character long for file name: " + fileName);
        }
        
        TableDefinition tableDefinition = datasource.tableDefinition();
        if (overwrite) {
            tableDefinition.deleteTable(qualifiedTableName);
        }
        // else make sure table does not exist
        else if (tableDefinition.tableExists(qualifiedTableName)) {
            throw new Exception("The table \"" + qualifiedTableName
                    + "\" already exists. Please select 'overwrite tables if exist' or choose a new table name.");
        }
        
        tableDefinition.createTable(qualifiedTableName, details.getColumnNames(), columnTypes, null);
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
                data = breakUpLine(line, details
                                .getColumnWidths());
                datasource.query().insertRow(qualifiedTableName, data, columnTypes);
                numRows++;
            }
        }// while file is not empty
        
        // perform capable table type specific processing
        postProcess(datasource, qualifiedTableName, tableType);
        
        // when all the data is done ingesting..
        // close the database connections by calling acceptor.finish..
        // and close the reader & writer as well..
        reader.close();
        if (writer != null)
            writer.close();
        
        if (kickOutRows > 0)
            System.out.println("Kicked out " + kickOutRows + " rows to file " + writerFileName);
    }

    private FileColumnsMetadata getDetails(File file) throws Exception {
        String fileName = file.getName();
        String fileImportType = fileName.substring(0, fileName.length() - 4);
        return fieldDefsReader.getFileColumnsMetadata(fileImportType);
    }

    public void createReferenceTables() throws Exception {
        String dbDir = null;
        File file = null;
        dbDir = referenceFilesDir.getPath() + File.separatorChar + REF_DIR_NAME;
        file = new File(dbDir);

        FilenameFilter textFileFilter = new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.indexOf(".txt") > 0) {
                    return true;
                }
                return false;
            }
        };
        final File[] files = file.listFiles(textFileFilter);
        
        Dataset dataset = new EmfDataset();
        final String datasetType = DatasetTypes.REFERENCE;
        dataset.addTable(ReferenceTable.REF_CONTROL_DEVICE_CODES);
        dataset.addTable(ReferenceTable.REF_CONVERSION_FACTORS);
        dataset.addTable(ReferenceTable.REF_EMISSION_TYPES);
        dataset.addTable(ReferenceTable.REF_EMISSION_UNITS_CODES);
        dataset.addTable(ReferenceTable.REF_FIPS);
        dataset.addTable(ReferenceTable.REF_MACT_CODES);
        dataset.addTable(ReferenceTable.REF_MATERIAL_CODES);
        dataset.addTable(ReferenceTable.REF_NAICS_CODES);
        dataset.addTable(ReferenceTable.REF_POLLUTANT_CODES);
        dataset.addTable(ReferenceTable.REF_SCC);
        dataset.addTable(ReferenceTable.REF_SIC_CODES);
        dataset.addTable(ReferenceTable.REF_TIME_ZONES);
        dataset.addTable(ReferenceTable.REF_TRIBAL_CODES);
        dataset.setDatasetType(datasetType);

        run(files, dataset, true);
    }

}
