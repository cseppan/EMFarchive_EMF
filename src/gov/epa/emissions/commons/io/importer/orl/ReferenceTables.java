package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Properties;

public class ReferenceTables {

    public static final String DELIMITER = ",";

    public static final String POLLUTANT_TABLE = "Pollutants";

    public static final int POLLUTANT_COL_COUNT = 2;

    public static final String COUNTRIES_TABLE = "Countries";

    public static final int COUNTRIES_COL_COUNT = 3;

    public static final String STATES_TABLE = "States";

    public static final int STATES_COL_COUNT = 4;

    public static final String SECTORS_TABLE = "Sectors";

    public static final int SECTORS_COL_COUNT = 1;

    public static final String COUNTY_TABLE = "FIPS";

    public static final int COUNTY_COL_COUNT = 20;

    private static final String ADD_REF_FILES_DIR = "addRefFiles";

    private File referenceFilesDir;

    private SqlTypeMapper sqlTypeMapper;

    public ReferenceTables(File referenceFilesDir, SqlTypeMapper sqlTypeMapper) {
        this.referenceFilesDir = referenceFilesDir;
        this.sqlTypeMapper = sqlTypeMapper;
    }

    public void createPollutantsTable(Datasource db) throws Exception {
        int col_count = POLLUTANT_COL_COUNT;
        String tableName = POLLUTANT_TABLE;
        String fileName = referenceFilesDir + "/" + ADD_REF_FILES_DIR + "/pollutants.txt";
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader bfr = new BufferedReader(fr);
        String line = null;

        String[] colTypes = { "VARCHAR(10)", "VARCHAR(32)" };
        String[] colNames = null;
        line = bfr.readLine();
        colNames = line.split(DELIMITER);
        String[] primaryCol = { colNames[0] };
        db.createTable(tableName, colNames, colTypes, primaryCol, true);
        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            line = line.trim();
            String[] pollutants = line.split(DELIMITER);
            if (pollutants.length != col_count) {
                throw new Exception("The file is not in the expected format");
            }
            db.insertRow(tableName, pollutants, colTypes);
        }
    }

    public void createCountriesTable(Datasource db) throws Exception {
        int col_count = COUNTRIES_COL_COUNT;
        String tableName = COUNTRIES_TABLE;
        String fileName = referenceFilesDir + "/" + ADD_REF_FILES_DIR + "/countries.txt";
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader bfr = new BufferedReader(fr);
        String line = null;

        String[] colTypes = { "VARCHAR(10)", "VARCHAR(10)", "VARCHAR(32)" };
        String[] colNames = null;
        line = bfr.readLine();
        colNames = line.split(",");
        String[] primaryCol = { colNames[0] };
        db.createTable(tableName, colNames, colTypes, primaryCol, true);

        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            line = line.trim();
            String[] countries = line.split(",");
            if (countries.length != col_count) {
                throw new Exception("The file is not in the expected format");
            }
            db.insertRow(tableName, countries, colTypes);
        }
    }

    public void createStatesTable(Datasource db) throws Exception {
        int col_count = STATES_COL_COUNT;
        String tableName = STATES_TABLE;
        String fileName = referenceFilesDir + "/" + ADD_REF_FILES_DIR + "/states.txt";
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader bfr = new BufferedReader(fr);
        String line = null;

        String[] colTypes = { "VARCHAR(10)", "VARCHAR(10)", "VARCHAR(10)", "VARCHAR(32)" };
        String[] colNames = null;
        line = bfr.readLine();
        colNames = line.split(",");
        String[] primaryCol = { colNames[0], colNames[1] };
        db.createTable(tableName, colNames, colTypes, primaryCol, true);

        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            line = line.trim();
            String[] countries = line.split(",");
            if (countries.length != col_count) {
                throw new Exception("The file is not in the expected format");
            }
            db.insertRow(tableName, countries, colTypes);
        }
    }

    public void createSectorsTable(Datasource db) throws Exception {
        int col_count = SECTORS_COL_COUNT;
        String tableName = SECTORS_TABLE;
        String fileName = referenceFilesDir + "/" + ADD_REF_FILES_DIR + "/sectors.txt";
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader bfr = new BufferedReader(fr);
        String line = null;

        String[] colTypes = { "VARCHAR(32)" };
        String[] colNames;
        line = bfr.readLine();
        colNames = line.split(",");
        String[] primaryCol = { colNames[0] };
        db.createTable(tableName, colNames, colTypes, primaryCol, true);

        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            line = line.trim();
            String[] sectors = line.split(",");
            if (sectors.length != col_count) {
                throw new Exception("The file is not in the expected format");
            }
            db.insertRow(tableName, sectors, colTypes);
        }
    }

    public void createCountyTable(Datasource db) throws Exception {
        int col_count = COUNTY_COL_COUNT;
        String tableName = COUNTY_TABLE;
        String fileName = referenceFilesDir + "/" + ADD_REF_FILES_DIR + "/counties.txt";
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader bfr = new BufferedReader(fr);
        String line = null;

        // TODO: inject the type mapper
        String[] colTypes = { "VARCHAR(8)", "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)",
                sqlTypeMapper.getSqlType("", "N", 0), sqlTypeMapper.getSqlType("", "N", 0),
                sqlTypeMapper.getSqlType("", "N", 0), sqlTypeMapper.getSqlType("", "N", 0),
                sqlTypeMapper.getSqlType("", "N", 0), sqlTypeMapper.getSqlType("", "N", 0),
                sqlTypeMapper.getSqlType("", "N", 0), "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(128)",
                "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)", };

        String[] colNames;
        line = bfr.readLine();
        colNames = line.split(",");
        // String[] primaryCol =
        // {colNames[0]};
        db.createTable(tableName, colNames, colTypes, null, true);
        int lineNo = 1;
        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            lineNo++;
            line = line.trim();
            String[] data = new String[col_count];
            Arrays.fill(data, "");
            String[] counties = line.split(",");
            if (counties.length > col_count) {
                throw new Exception("The file is not in the expected format, line no=" + lineNo);
            } else {
                for (int i = 0; i < counties.length; i++) {
                    data[i] = counties[i].trim();
                }
            }
            db.insertRow(tableName, data, colTypes);
        }
    }

    public void createAdditionRefTables(Datasource referenceDatasource) throws Exception {
        System.out.println("Creating additional reference tables:\n");

        createPollutantsTable(referenceDatasource);
        createCountriesTable(referenceDatasource);
        createStatesTable(referenceDatasource);
        createSectorsTable(referenceDatasource);
        createCountyTable(referenceDatasource);

        System.out.println("Sucessfully created additional reference tables\n");
    }

    public static void main(String[] args) throws Exception {
        DefaultUserInteractor.set(new GUIUserInteractor());
        
        // common setup for importers
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("test/user_preferences.txt")));
        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");
        
        DatabaseSetup dbSetup = new DatabaseSetup(properties);
        dbSetup.init();
        // end setup
        
        ReferenceTables tables = new ReferenceTables(null, dbSetup.getDbServer().getTypeMapper());
        tables.createAdditionRefTables(dbSetup.getDbServer().getReferenceDatasource());
    }// main

}
