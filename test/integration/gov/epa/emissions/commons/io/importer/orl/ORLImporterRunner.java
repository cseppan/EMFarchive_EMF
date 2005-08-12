package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.TableTypes;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ORLImporterRunner {

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("test/user_preferences.txt")));
        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");

        DatabaseSetup dbSetup = new DatabaseSetup(properties);
        dbSetup.init();

        if (args.length != 2) {
            String a = "nonpoint";
            String b = "test/data/orl/nc/arinv.nonpoint.nti99_NC.txt";
            System.out.println("Incorrect number of command line arguments specified...");
            System.out.println("Usage: java gov.epa.emissions.emisview.io.importer.ORLImporter datasetType fileName");
            System.out.println("\tdatasetType\t\"nonpoint\", \"nonroad\", \"point\", \"mobile\", or \"onroad\"");
            System.out
                    .println("\tfileName\tthe name of the file to import (file name is used to determine table name)");
            System.out.println("trying: java gov.epa.emissions.emisview.io.importer.ORLImporter " + a + " " + b);
            args = new String[] { a, b };
        }

        final String datasetType;
        final String tableType;
        final String dbFile;
        String typeToken = args[0];
        if (typeToken.equalsIgnoreCase("nonpoint")) {
            datasetType = DatasetTypes.ORL_AREA_NONPOINT_TOXICS;
            tableType = TableTypes.ORL_AREA_NONPOINT_TOXICS;
            // dbFile = "arinv.nonpoint.nti99_NC.100.txt";
        } else if (typeToken.equalsIgnoreCase("nonroad")) {
            datasetType = DatasetTypes.ORL_AREA_NONROAD_TOXICS;
            tableType = TableTypes.ORL_AREA_NONROAD_TOXICS;
            // final String dbFile = "arinv.nonroad.nti99d_NC.new.100.txt";
        } else if (typeToken.equalsIgnoreCase("point")) {
            datasetType = DatasetTypes.ORL_POINT_TOXICS;
            tableType = TableTypes.ORL_POINT_TOXICS;
            // dbFile = "ptinv.nti99_NC.100.txt";
        } else if (typeToken.equalsIgnoreCase("mobile") || typeToken.equalsIgnoreCase("onroad")) {
            datasetType = DatasetTypes.ORL_MOBILE_TOXICS;
            tableType = TableTypes.ORL_MOBILE_TOXICS;
            // final String dbFile = "nti99.NC.onroad.SMOKE.100.txt";
        } else {
            datasetType = null;
            tableType = null;
        }
        dbFile = args[1];

        final File[] files = { new File(dbFile) };
        final String dbFileName = files[0].getName();
        final String tableName = dbFileName.substring(0, dbFileName.length() - 4).replace('.', '_');

        long startTime = System.currentTimeMillis();
        
        Dataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        dataset.addDataTable(tableType, tableName);
        dataset.addDataTable(DatasetTypes.getSummaryTableType(datasetType), tableName + "_summary");
        
        ORLImporter importer = new ORLImporter(dbSetup.getDbServer(), false, true);
        importer.putIntoDatabase(files, true, dataset);
        
        long stopTime = System.currentTimeMillis();        
        System.out.println("The total time required was " + ((stopTime - startTime) / 1000) + " seconds");
    }
}
