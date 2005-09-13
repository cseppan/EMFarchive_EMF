package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.SqlTypeMapper;
import gov.epa.emissions.commons.db.TableDefinition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

public class ReferenceTablesCreator {

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

    public ReferenceTablesCreator(File referenceFilesDir, SqlTypeMapper sqlTypeMapper) {
        this.referenceFilesDir = referenceFilesDir;
        this.sqlTypeMapper = sqlTypeMapper;
    }

    public void createPollutantsTable(Datasource datasource) throws Exception {
        int col_count = POLLUTANT_COL_COUNT;
        String qualifiedTableName = datasource.getName() + "." + POLLUTANT_TABLE;

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
        TableDefinition tableDefinition = datasource.tableDefinition();
        tableDefinition.createTableWithOverwrite(qualifiedTableName, colNames, colTypes, primaryCol);
        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            line = line.trim();
            String[] pollutants = line.split(DELIMITER);
            if (pollutants.length != col_count) {
                throw new Exception("The file is not in the expected format");
            }
            datasource.query().insertRow(qualifiedTableName, pollutants, colTypes);
        }
    }

    public void createCountriesTable(Datasource datasource) throws Exception {
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
        String qualifiedTableName = datasource.getName() + "." + tableName;
        datasource.tableDefinition().createTableWithOverwrite(qualifiedTableName, colNames, colTypes, primaryCol);

        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            line = line.trim();
            String[] countries = line.split(",");
            if (countries.length != col_count) {
                throw new Exception("The file is not in the expected format");
            }
            datasource.query().insertRow(qualifiedTableName, countries, colTypes);
        }
    }

    public void createStatesTable(Datasource datasource) throws Exception {
        int col_count = STATES_COL_COUNT;
        String qualifiedTableName = datasource.getName() + "." + STATES_TABLE;

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
        datasource.tableDefinition().createTableWithOverwrite(qualifiedTableName, colNames, colTypes, primaryCol);

        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            line = line.trim();
            String[] countries = line.split(",");
            if (countries.length != col_count) {
                throw new Exception("The file is not in the expected format");
            }
            datasource.query().insertRow(qualifiedTableName, countries, colTypes);
        }
    }

    public void createSectorsTable(Datasource datasource) throws Exception {
        int col_count = SECTORS_COL_COUNT;
        String qualifiedTableName = datasource.getName() + "." + SECTORS_TABLE;
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
        datasource.tableDefinition().createTableWithOverwrite(qualifiedTableName, colNames, colTypes, primaryCol);

        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            line = line.trim();
            String[] sectors = line.split(",");
            if (sectors.length != col_count) {
                throw new Exception("The file is not in the expected format");
            }
            datasource.query().insertRow(qualifiedTableName, sectors, colTypes);
        }
    }

    public void createCountyTable(Datasource datasource) throws Exception {
        int col_count = COUNTY_COL_COUNT;
        String tableName = COUNTY_TABLE;
        String fileName = referenceFilesDir + "/" + ADD_REF_FILES_DIR + "/counties.txt";
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader bfr = new BufferedReader(fr);
        String line = null;

        String[] colTypes = { "VARCHAR(8)", "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)",
                sqlTypeMapper.getSqlType("", "N", 0), sqlTypeMapper.getSqlType("", "N", 0),
                sqlTypeMapper.getSqlType("", "N", 0), sqlTypeMapper.getSqlType("", "N", 0),
                sqlTypeMapper.getSqlType("", "N", 0), sqlTypeMapper.getSqlType("", "N", 0),
                sqlTypeMapper.getSqlType("", "N", 0), "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(128)",
                "VARCHAR(32)", "VARCHAR(32)", "VARCHAR(32)", };

        String[] colNames;
        line = bfr.readLine();
        colNames = line.split(",");
        String qualifiedTableName = datasource.getName() + "." + tableName;
        datasource.tableDefinition().createTableWithOverwrite(qualifiedTableName, colNames, colTypes, null);
        int lineNo = 1;
        while ((line = bfr.readLine()) != null && line.trim().length() != 0) {
            lineNo++;
            line = line.trim();
            String[] data = new String[col_count];
            Arrays.fill(data, "");
            String[] counties = line.split(",");
            if (counties.length > col_count) {
                throw new Exception("The file is not in the expected format, line no=" + lineNo);
            }
           
            for (int i = 0; i < counties.length; i++) {
                data[i] = counties[i].trim();
            }
            datasource.query().insertRow(qualifiedTableName, data, colTypes);
        }
    }

    public void createAdditionRefTables(Datasource referenceDatasource) throws Exception {
        createPollutantsTable(referenceDatasource);
        createCountriesTable(referenceDatasource);
        createStatesTable(referenceDatasource);
        createSectorsTable(referenceDatasource);
        createCountyTable(referenceDatasource);
    }

}
