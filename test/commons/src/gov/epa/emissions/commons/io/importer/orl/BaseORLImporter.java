package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.DataAcceptor;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.io.ColumnType;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.FileColumnsMetadata;
import gov.epa.emissions.commons.io.importer.ListFormatImporter;
import gov.epa.emissions.commons.io.importer.ORLTableTypes;
import gov.epa.emissions.commons.io.importer.SummaryTableCreator;
import gov.epa.emissions.commons.io.importer.TableType;
import gov.epa.emissions.commons.io.importer.TemporalResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The importer for ORL (One Record per Line) format text files.
 */
public class BaseORLImporter extends ListFormatImporter {
    private static Log log = LogFactory.getLog(BaseORLImporter.class);

    /* ORL header record command fields */
    private static final String COMMAND = "#";

    private static final String TOXICS_COMMAND = COMMAND + "TOXICS";

    private static final String ORL_COMMAND = COMMAND + "ORL";

    private static final String TOXICS_NONPOINT = "NONPOINT";

    private static final String TYPE_COMMAND = COMMAND + "TYPE";

    private static final String COUNTRY_COMMAND = COMMAND + "COUNTRY";

    private static final String YEAR_COMMAND = COMMAND + "YEAR";

    private static final String DESCRIPTION_COMMAND = COMMAND + "DESC";

    /** primary column: annual emissions or average daily emissions */
    protected boolean annualNotAverageDaily = true;

    /** the ORL data format * */
    private ORLDataFormat orlDataFormat = null;

    private boolean toxicsCommandRead = false;

    private boolean extendedFormat = false;

    private String fileType = null;

    private String countryName = null;

    private String dataYear = null;

    private List dataDescriptions = null;

    private List comments = null;

    /* read ahead limit ~ 100 MB */
    public static final long READ_AHEAD_LIMIT = 105000000L;

    public BaseORLImporter(DbServer dbServer, boolean useTransactions, boolean annualNotAverageDaily) {
        super(new ORLTableTypes(), dbServer, ListFormatImporter.WHITESPACE_REGEX, useTransactions, true);
        this.annualNotAverageDaily = annualNotAverageDaily;
    }

    /**
     * Take a array of Files and put them database, overwriting existing
     * corresponding tables specified in dataset based on overwrite flag.
     * 
     * @param files -
     *            an array of Files which are checked prior to import
     * @param dataset -
     *            Dataset specifying needed properties such as datasetType and
     *            table name (table name look-up is based on file name)
     * @param overwrite -
     *            whether or not to overwrite corresponding tables
     */
    public void run(File[] files, Dataset dataset, boolean overwrite) throws Exception {
        this.dataset = dataset;

        Datasource datasource = dbServer.getEmissionsDatasource();
        String type = dataset.getDatasetType();

        // remove extra files, then check for and return files necessary for
        // import
        files = checkFiles(type, files);
        // checkFiles() implicitly returns a File array of length one for ORL
        // explicitly make sure only one valid file is returned
        if (files.length != 1) {
            throw new Exception("Can only import one valid orl file at a time: " + files);
        }

        if (!type.equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS) && !type.equals(DatasetTypes.ORL_AREA_NONROAD_TOXICS)
                && !type.equals(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS) && !type.equals(DatasetTypes.ORL_POINT_TOXICS)) {
            throw new Exception("Unknown/unhandled ORL type: " + type);
        }

        // set the data source for the dataset
        setDataSources(files);

        // import the file
        importFile(files[0], datasource, type, overwrite);

        // perform post import changes
        postImport();
    }

    /**
     * import a single file into the specified database
     * 
     * @param file -
     *            the file to be ingested in
     * @param dbName -
     *            the database into which the data is ingested from the file
     * @param details -
     *            the details with which to import the file
     */
    public void importFile(File file, Datasource datasource, String datasetType, boolean overwrite) throws Exception {
        // get a bufferedreader for the file to be imported in
        BufferedReader reader = new BufferedReader(new FileReader(file));

        long fileLength = file.length() + 1;
        long lastModified = file.lastModified();
        // if file is small enough, mark file read ahead limit from beginning
        // so we can come back without having to close and reopen the file
        if (fileLength < BaseORLImporter.READ_AHEAD_LIMIT) {
            reader.mark((int) fileLength);
        }

        // read the header commands
        initializeHeaders();
        readHeader(reader);
        checkHeaders(file.getAbsolutePath());

        // if able, go back to the file beginning
        if (fileLength < BaseORLImporter.READ_AHEAD_LIMIT) {
            reader.reset();
        }
        // else close, reopen and check for modification
        else {
            // close the file
            reader.close();
            // reopen the file
            reader = new BufferedReader(new FileReader(file));
            // check the file for modification
            long currentLastModified = file.lastModified();
            if (lastModified != currentLastModified) {
                reader.close();
                throw new Exception("File " + file.getAbsolutePath()
                        + " changed during import. Do not edit file while import is executing.");
            }
        }

        FileColumnsMetadata metadata = getDetails(datasetType);
        String[] columnNames = metadata.getColumnNames();
        String[] columnTypes = metadata.getColumnTypes();
        int[] columnWidths = metadata.getColumnWidths();

        doImport(file, datasource, reader, columnNames, columnTypes, columnWidths, overwrite);

        // set dataset variables not specified in files
        final String unitsValue = "short tons";
        final String annualUnits = unitsValue + "/year";
        final String averageDailyUnits = unitsValue + "/day";
        if (annualNotAverageDaily) {
            dataset.setUnits(annualUnits);
            dataset.setTemporalResolution(TemporalResolution.ANNUAL.getName());
        } else {
            dataset.setUnits(averageDailyUnits);
            dataset.setTemporalResolution(TemporalResolution.DAILY.getName());
        }
    }

    private String doImport(File file, Datasource datasource, BufferedReader reader, String[] columnNames,
            String[] columnTypes, int[] columnWidths, boolean overwrite) throws Exception {
        String fileName = file.getName();
        String datasetType = dataset.getDatasetType();
        TableType tableType = tableTypes.type(datasetType);
        if (tableType == null) {
            throw new Exception("Could not determine table type for file name: " + fileName);
        }

        // use the table type to get the table name
        String baseTableType = tableType.baseTypes()[0];
        Table table = dataset.getTable(baseTableType);
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
            log.error("The table \"" + qualifiedTableName
                    + "\" already exists. Please select 'overwrite tables if exist' or choose a new table name.");
            throw new Exception("The table \"" + qualifiedTableName
                    + "\" already exists. Please select 'overwrite tables if exist' or choose a new table name.");
        }

        tableDefinition.createTable(qualifiedTableName, columnNames, columnTypes, null);
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
                datasource.query().insertRow(qualifiedTableName, data, columnTypes);
                numRows++;
            }
        }// while file is not empty

        // perform capable table type specific processing
        postProcess(datasource, qualifiedTableName, baseTableType);

        // when all the data is done ingesting..
        // close the database connections by calling acceptor.finish..
        // and close the reader & writer as well..
        reader.close();
        if (writer != null)
            writer.close();

        if (kickOutRows > 0)
            System.out.println("Kicked out " + kickOutRows + " rows to file " + writerFileName);

        return tableName;
    }

    private void initializeHeaders() {
        toxicsCommandRead = false;
        extendedFormat = false;
        fileType = null;
        countryName = null;
        dataYear = null;
        dataDescriptions = new ArrayList/* <String> */();
        comments = new ArrayList/* <String> */();
    }// initializeHeaders()

    private void checkHeaders(String fileName) throws Exception {
        String command = null;
        if (!toxicsCommandRead) {
            command = TOXICS_COMMAND;
        } else if (fileType == null) {
            command = TYPE_COMMAND;
        } else if (countryName == null) {
            command = COUNTRY_COMMAND;
        } else if (dataYear == null) {
            command = YEAR_COMMAND;
        }

        if (command != null) {
            throw new Exception("Missing header command \"" + command + "\" from file " + fileName);
        }
    }

    private void readHeader(BufferedReader reader) throws Exception {
        String line = null;

        // read lines in one at a time and put the data into the database.
        while ((line = reader.readLine()) != null) {
            // if header command
            if (line.startsWith(COMMAND)) {
                readHeaderLine(line);
            }
        }// while file is not empty
    }// readHeader(File, BufferedReader, FileImportDetails)

    /**
     * Take a header command line and read it.
     * 
     * @param line -
     *            the header line to read
     * @param details -
     *            the details with which to read the header with
     * @param acceptor -
     *            the database connection acceptor
     * @throws Exception
     */
    private void readHeaderLine(String line) throws Exception {
        String[] tokens = line.split("\\s+");
        String command = tokens[0].intern();

        // #TOXICS
        // #TOXICS NONPOINT
        // #ORL
        // #ORL NONPOINT
        if ((command.equals(TOXICS_COMMAND) || command.equals(ORL_COMMAND)) && tokens.length <= 2 && !toxicsCommandRead) {
            if (tokens.length == 2) {
                if (!dataset.getDatasetType().equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS)) {
                    throw new Exception("\"" + command + " " + TOXICS_NONPOINT
                            + "\" is an invalid header command for dataset type \"" + dataset.getDatasetType() + "\"");
                }

                String nonPoint = tokens[1].intern();
                if (!nonPoint.equals(TOXICS_NONPOINT)) {
                    throw new Exception("Expected \"" + TOXICS_NONPOINT + "\" after header command \"" + command
                            + "\" but found \"" + nonPoint + "\" instead");
                }
            }
            if (command.equals(ORL_COMMAND)) {
                extendedFormat = true;
            }
            toxicsCommandRead = true;
        }
        // #TOXICS
        // #TOXICS NONPOINT
        // #ORL
        // #ORL NONPOINT

        // #TYPE fileType
        else if (command.equals(TYPE_COMMAND) && tokens.length > 1 && fileType == null) {
            // final char SPACE = '\u0020';
            fileType = line.substring(TYPE_COMMAND.length()).trim();
            checkDatasetType(dataset.getDatasetType(), fileType);
        }// #TYPE fileType

        // #COUNTRY countryName
        else if (command.equals(COUNTRY_COMMAND) && tokens.length == 2 && countryName == null) {
            countryName = tokens[1];
            dataset.setRegion(countryName);
        }// #COUNTRY countryName

        // #YEAR dataYear
        else if (command.equals(YEAR_COMMAND) && tokens.length == 2 && dataYear == null) {
            dataYear = tokens[1];
            int year = Integer.parseInt(dataYear);
            dataset.setYear(year);

            // get localized DateFormat and corresponding Calendar
            DateFormat dateFormat = DateFormat.getDateInstance();
            Calendar calendar = dateFormat.getCalendar();
            calendar.set(Calendar.YEAR, year);

            // start date (January 1, Year Midnight)
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            dataset.setStartDateTime(calendar.getTime());

            // stop date (December 31, Year one millisecond before Midnight)
            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
            calendar.set(Calendar.DAY_OF_MONTH, 31);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            dataset.setStopDateTime(calendar.getTime());
        }// #YEAR dataYear

        // #DESC description
        else if (command.equals(DESCRIPTION_COMMAND) && tokens.length > 1) {
            String description = line.substring(DESCRIPTION_COMMAND.length()).trim();
            // multiple description lines are allowed: add to List for later
            dataDescriptions.add(description);
        }// #DESC description

        else {
            // This is a comment. Add to list and ignore.
            comments.add(line);
        }
    }// readHeaderLine(String, FileImportDetails)

    private void checkDatasetType(String datasetType, String fileType) throws Exception {
        String keyword = null;
        String fileTypeLowerCase = fileType.toLowerCase();
        if (datasetType.equals(DatasetTypes.ORL_AREA_NONROAD_TOXICS) && fileTypeLowerCase.indexOf("nonroad") == -1) {
            keyword = "Nonroad";
        } else if (datasetType.equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS)
                && (fileTypeLowerCase.indexOf("nonpoint") == -1) && fileTypeLowerCase.indexOf("non-point") == -1) {
            // must check for "Nonpoint" before check for "Point"
            keyword = "Nonpoint";
        } else if (datasetType.equals(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS)
                && fileTypeLowerCase.indexOf("mobile") == -1) {
            keyword = "Mobile";
        } else if (datasetType.equals(DatasetTypes.ORL_POINT_TOXICS) && fileTypeLowerCase.indexOf("point") == -1) {
            // must check for "Nonpoint" before check for "Point"
            keyword = "Point";
        }

        if (keyword != null) {
            throw new Exception("File type \"" + fileType + "\" must contain the word \"" + keyword
                    + "\" to be valid for dataset type \"" + dataset.getDatasetType() + "\"");
        }
    }

    /**
     * get the file import details for a particular file
     * 
     * @param file -
     *            the file to find FileImportDetails for
     * @return FileImportDetails for the file
     */
    private FileColumnsMetadata getDetails(String datasetType) throws Exception {
        if (datasetType.equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS)) {
            orlDataFormat = new ORLAreaNonpointDataFormat(dbServer.getTypeMapper(), extendedFormat);
        } else if (datasetType.equals(DatasetTypes.ORL_AREA_NONROAD_TOXICS)) {
            orlDataFormat = new ORLAreaNonroadDataFormat(dbServer.getTypeMapper(), extendedFormat);
        } else if (datasetType.equals(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS)) {
            orlDataFormat = new ORLMobileDataFormat(dbServer.getTypeMapper(), extendedFormat);
        } else if (datasetType.equals(DatasetTypes.ORL_POINT_TOXICS)) {
            orlDataFormat = new ORLPointDataFormat(dbServer.getTypeMapper(), extendedFormat);
        } else {
            orlDataFormat = null;
            throw new Exception("Unknown ORL file type: " + datasetType);
        }

        return orlDataFormat.getFileImportDetails();
    }

    protected void postProcess(Datasource datasource, String table, String tableType) throws Exception {
        TableDefinition tableDefinition = datasource.tableDefinition();

        // FIXME: ORL table type has only ONE base type, unlike others (NIF).
        // Use ORLTableType object

        // point
        if (tableType.equals(ORLTableTypes.ORL_POINT_TOXICS.baseTypes()[0])) {
            String[] indexColumnNames = { ORLDataFormat.FIPS_NAME, ORLPointDataFormat.PLANT_ID_CODE_NAME,
                    ORLPointDataFormat.POINT_ID_CODE_NAME, ORLPointDataFormat.STACK_ID_CODE_NAME,
                    ORLPointDataFormat.DOE_PLANT_ID_NAME, ORLPointDataFormat.SOURCE_CLASSIFICATION_CODE_NAME };
            tableDefinition.addIndex(table, "orl_point_key", indexColumnNames);
        }
        // nonpoint
        if (tableType.equals(ORLTableTypes.ORL_AREA_NONPOINT_TOXICS.baseTypes()[0])) {
            String[] indexColumnNames = { ORLDataFormat.FIPS_NAME,
                    ORLAreaNonpointDataFormat.SOURCE_CLASSIFICATION_CODE_NAME };
            tableDefinition.addIndex(table, "orl_nonpoint_key", indexColumnNames);
        }
        // nonroad
        if (tableType.equals(ORLTableTypes.ORL_AREA_NONROAD_TOXICS.baseTypes()[0])) {
            String[] indexColumnNames = { ORLDataFormat.FIPS_NAME,
                    ORLAreaNonroadDataFormat.SOURCE_CLASSIFICATION_CODE_NAME };
            tableDefinition.addIndex(table, "orl_nonroad_key", indexColumnNames);
        }
        // mobile/onroad
        if (tableType.equals(ORLTableTypes.ORL_ONROAD_MOBILE_TOXICS.baseTypes()[0])) {
            String[] indexColumnNames = { ORLDataFormat.FIPS_NAME, ORLMobileDataFormat.SOURCE_CLASSIFICATION_CODE_NAME };
            tableDefinition.addIndex(table, "orl_mobile_key", indexColumnNames);
        }

        // set the description, combining multiple lines into one String
        String description = null;
        if (dataDescriptions.size() > 0) {
            Iterator it = dataDescriptions.iterator();
            StringBuffer sb = new StringBuffer((String) it.next());
            while (it.hasNext()) {
                sb.append("\n").append((String) it.next());
            }
            description = sb.toString();
        }
        dataset.setDescription(description);
    }

    /**
     * Perform post import operations on the data set most recently added into
     * the database.
     */
    private void postImport() throws Exception {
        Datasource emissionsDatasource = dbServer.getEmissionsDatasource();
        DataAcceptor emissionsAcceptor = emissionsDatasource.getDataAcceptor();
        // ORL table types
        String datasetType = dataset.getDatasetType();
        TableType tableType = tableTypes.type(datasetType);
        // only one base type.
        // FIXME: why not have a ORLTableType that only has one base table ?
        Table table = dataset.getTable(tableType.baseTypes()[0]);
        String qualifiedTableName = emissionsDatasource.getName() + "." + table.getTableName();

        final String FIPS_NAME = modifyFipsColumn(emissionsDatasource, emissionsAcceptor, tableType, qualifiedTableName);
        modifyStateColumn(emissionsDatasource, emissionsAcceptor, qualifiedTableName, FIPS_NAME);
    }

    private void modifyStateColumn(Datasource emissionsDatasource, DataAcceptor emissionsAcceptor,
            String qualifiedTableName, final String FIPS_NAME) throws Exception, SQLException {
        // artificially insert the STATE data column, a four
        // character String from the reference.fips table
        final String STATE_NAME = "state_abbr";
        final int STATE_WIDTH = 4;
        final ColumnType STATE_TYPE = ColumnType.CHAR;
        FileColumnsMetadata state = new FileColumnsMetadata(STATE_NAME, dbServer.getTypeMapper());
        state.addColumnName(STATE_NAME);

        state.setWidth(STATE_NAME, String.valueOf(STATE_WIDTH));
        state.setType(STATE_NAME, STATE_TYPE.getName());

        // STATE column
        emissionsDatasource.tableDefinition().addColumn(qualifiedTableName, STATE_NAME, state.getType(STATE_NAME),
                FIPS_NAME);

        // update STATE column
        /**
         * the algorithm used below is more complex than letting SQL do a
         * subquery joining the table and reference.fips table. Since the tables
         * are so huge the inner join is a time bottleneck. By figuring out all
         * the possible state abbreviations in reference.fips, all the state
         * codes used in the current table, and then issuing an update for each
         * one, our speed improvement is several orders of magnitude, i.e. 2-3
         * mins max versus dozens of minutes and even hours.
         */
        Datasource referenceDatasource = dbServer.getReferenceDatasource();
        final String FIPS_TABLE_NAME = referenceDatasource.getName() + ".fips";
        final String[] fipsSelectColumns = { "DISTINCT " + SummaryTableCreator.STATE_COL, "country_code",
                "FLOOR(" + dbServer.asciiToNumber(SummaryTableCreator.FIPS_COL, 5) + "/1000) AS state_code" };
        // select state abbreviations, country codes and state codes from
        // reference.fips table
        ResultSet results = referenceDatasource.query().select(fipsSelectColumns, FIPS_TABLE_NAME);
        // use results to create double level map
        // first level -> country code to code-abbreviation map
        Map countryToStateCodeMap = new HashMap();
        while (results.next()) {
            String state_abbr = results.getString(1);
            String country_code = results.getString(2);
            String state_code = results.getString(3);
            // second level -> state code to state abbreviation
            Map stateCodeToStateAbbrMap = (Map) countryToStateCodeMap.get(country_code);
            if (stateCodeToStateAbbrMap == null) {
                stateCodeToStateAbbrMap = new HashMap();
                countryToStateCodeMap.put(country_code, stateCodeToStateAbbrMap);
            }
            if (stateCodeToStateAbbrMap.put(state_code, state_abbr) != null) {
                throw new Exception("Duplicate state code '" + state_code + "' in country '" + country_code
                        + "' for table " + FIPS_TABLE_NAME);
            }
        }

        String fipsVal = dbServer.asciiToNumber(ORLDataFormat.FIPS_NAME, 5);
        final String[] usedStateCodesSelectColumns = { "DISTINCT FLOOR(" + fipsVal + "/1000) AS state_code" };

        results = emissionsDatasource.query().select(usedStateCodesSelectColumns, qualifiedTableName);
        // we only need to issue SQL update commands for used state codes
        List usedStateCodes = new ArrayList();
        while (results.next()) {
            // due to the nature of ResultSet, we must iterate through
            // all the rows and extract the information before we can
            // issue another SQL command.
            usedStateCodes.add(results.getString(1));
        }// while(results.next())
        Map stateCodeToStateAbbrMap = (Map) countryToStateCodeMap.get(dataset.getRegion());
        Iterator it = usedStateCodes.iterator();
        while (it.hasNext()) {
            String stateCode = (String) it.next();
            String stateAbbr = (String) stateCodeToStateAbbrMap.get(stateCode);
            String[] whereColumns = { "FLOOR(" + dbServer.asciiToNumber(ORLDataFormat.FIPS_NAME, 5) + "/1000)" };
            String[] equalsClauses = { stateCode };

            // update
            emissionsAcceptor.updateWhereEquals(qualifiedTableName, STATE_NAME, "'" + stateAbbr + "'", whereColumns,
                    equalsClauses);
        }
    }

    private String modifyFipsColumn(Datasource emissionsDatasource, DataAcceptor emissionsAcceptor,
            TableType tableType, String qualifiedTableName) throws Exception {
        // artificially insert the FIPS data column, a five
        // character String concatenating the state and county codes
        final String FIPS_NAME = ORLDataFormat.FIPS_NAME;
        // FIPS column
        if (!extendedFormat
                && (tableType.equals(ORLTableTypes.ORL_AREA_NONROAD_TOXICS) || tableType
                        .equals(ORLTableTypes.ORL_ONROAD_MOBILE_TOXICS))) {
            final int FIPS_WIDTH = 5;
            final ColumnType FIPS_TYPE = ColumnType.CHAR;
            FileColumnsMetadata fips = new FileColumnsMetadata(FIPS_NAME, dbServer.getTypeMapper());
            fips.addColumnName(FIPS_NAME);

            fips.setWidth(FIPS_NAME, String.valueOf(FIPS_WIDTH));
            fips.setType(FIPS_NAME, FIPS_TYPE.getName());

            final String STATE_CODE_NAME = "STATE";
            final int STATE_CODE_WIDTH = 2;
            final String COUNTY_CODE_NAME = "COUNTY";
            final int COUNTY_CODE_WIDTH = 3;

            // alter table
            emissionsDatasource.tableDefinition().addColumn(qualifiedTableName, FIPS_NAME, fips.getType(FIPS_NAME),
                    COUNTY_CODE_NAME);

            // update FIPS column
            for (int stid = 0; stid < STATE_CODE_WIDTH; stid++) {
                // set-up parameters for SQL update
                StringBuffer stidLike = new StringBuffer("__");
                StringBuffer stidConcat = new StringBuffer("0");
                stidLike.delete(1, stidLike.length() - stid);
                stidConcat.delete(0, stid);
                for (int cyid = 0; cyid < COUNTY_CODE_WIDTH; cyid++) {
                    StringBuffer cyidLike = new StringBuffer("___");
                    StringBuffer cyidConcat = new StringBuffer("00");
                    cyidLike.delete(1, cyidLike.length() - cyid);
                    cyidConcat.delete(0, cyid);
                    String[] concatExprs = { "'" + stidConcat + "'", STATE_CODE_NAME, "'" + cyidConcat + "'",
                            COUNTY_CODE_NAME };
                    String concatExpr = emissionsAcceptor.generateConcatExpr(concatExprs);
                    String[] whereColumns = { STATE_CODE_NAME, COUNTY_CODE_NAME };
                    String[] likeClauses = { stidLike.toString(), cyidLike.toString() };

                    // update
                    emissionsAcceptor.updateWhereLike(qualifiedTableName, FIPS_NAME, concatExpr, whereColumns,
                            likeClauses);
                }
            }
        }
        return FIPS_NAME;
    }

    public List getComments() {
        return comments;
    }
}
