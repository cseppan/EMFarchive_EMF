package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

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
        super(dbServer);
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
            System.out.println("importing file - " + files[i]);
            importFile(files[i], datasource, getDetails(files[i]), overwrite);
            System.out.println("successfully imported file - " + files[i]);
        }
    }

    /**
     * import a single file into the specified database
     */
    public void importFile(File file, Datasource datasource, FileColumnsMetadata details, boolean overwrite)
            throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        super.importFile(file, datasource, reader, details.getColumnNames(), details.getColumnTypes(), details
                .getColumnWidths(), overwrite);
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
        dataset.addDataTable(TableTypes.REF_CONTROL_DEVICE_CODES, "control_device_codes");
        dataset.addDataTable(TableTypes.REF_CONVERSION_FACTORS, "conversion_factors");
        dataset.addDataTable(TableTypes.REF_EMISSION_TYPES, "emission_types");
        dataset.addDataTable(TableTypes.REF_EMISSION_UNITS_CODES, "emission_units_codes");
        dataset.addDataTable(TableTypes.REF_FIPS, "fips");
        dataset.addDataTable(TableTypes.REF_MACT_CODES, "mact_codes");
        dataset.addDataTable(TableTypes.REF_MATERIAL_CODES, "material_codes");
        dataset.addDataTable(TableTypes.REF_NAICS_CODES, "naics_codes");
        dataset.addDataTable(TableTypes.REF_POLLUTANT_CODES, "pollutant_codes");
        dataset.addDataTable(TableTypes.REF_SCC, "scc");
        dataset.addDataTable(TableTypes.REF_SIC_CODES, "sic_codes");
        dataset.addDataTable(TableTypes.REF_TIME_ZONES, "time_zones");
        dataset.addDataTable(TableTypes.REF_TRIBAL_CODES, "tribal");
        dataset.setDatasetType(datasetType);

        run(files, dataset, true);
    }

}
