package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.io.ColumnType;
import gov.epa.emissions.commons.io.Constants;
import gov.epa.emissions.commons.io.DataAcceptor;
import gov.epa.emissions.commons.io.Database;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetTypes;
import gov.epa.emissions.commons.io.MySQLDataAcceptor;
import gov.epa.emissions.commons.io.TableModifier;
import gov.epa.emissions.commons.io.TableTypes;
import gov.epa.emissions.commons.io.TemporalResolution;
import gov.epa.emissions.commons.io.importer.FileImportDetails;
import gov.epa.emissions.commons.io.importer.ListFormatImporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The importer for ORL (One Record per Line) format text files.
 * @author Keith Lee, CEP UNC
 * @version $Id: ORLImporter.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 */
public class ORLImporter extends ListFormatImporter
{
    /* ORL header record command fields */
    private static final String COMMAND = "#";
    private static final String TOXICS_COMMAND       = COMMAND + "TOXICS";
    private static final String ORL_COMMAND          = COMMAND + "ORL";
    private static final String TOXICS_NONPOINT      = "NONPOINT";
    private static final String TYPE_COMMAND         = COMMAND + "TYPE";
    private static final String COUNTRY_COMMAND      = COMMAND + "COUNTRY";
    private static final String YEAR_COMMAND         = COMMAND + "YEAR";
    private static final String DESCRIPTION_COMMAND  = COMMAND + "DESC";

    /** primary column: annual emissions or average daily emissions */
    private boolean annualNotAverageDaily = true;

    private boolean toxicsCommandRead = false;
    private boolean extendedFormat = false;
    private String fileType = null;
    private String countryName = null;
    private String dataYear = null;
    private List dataDescriptions = null;
    private List comments = null;
    private Database database;

    public ORLImporter(boolean useTransactions, boolean annualNotAverageDaily)
    {
        super(ListFormatImporter.WHITESPACE_REGEX, useTransactions, true);
        this.annualNotAverageDaily = annualNotAverageDaily;
    }

    /**
     * Take a array of Files and put them database, overwriting existing
     * corresponding tables specified in dataset based on overwrite flag.
     * @param files - an array of Files which are checked prior to import
     * @param overwrite - whether or not to overwrite corresponding tables
     * @param dataset - Dataset specifying needed properties such as
     *                  datasetType and table name (table name look-up is
     *                  based on file name)
     */
    public void putIntoDatabase(File[] files, boolean overwrite, Dataset dataset) throws Exception
    {
        this.dataset = dataset;

        String dbName = database.getName();
        String type = dataset.getDatasetType();

        //show import status
        if(Constants.SHOW_STATUS)
            System.out.println("Importing dataset type \"" + type + "\"" + " at "
                    + java.util.Calendar.getInstance().getTime());

        files = checkFiles(type, files);

        if(!type.equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS) &&
           !type.equals(DatasetTypes.ORL_AREA_NONROAD_TOXICS) &&
           !type.equals(DatasetTypes.ORL_MOBILE_TOXICS) &&
           !type.equals(DatasetTypes.ORL_POINT_TOXICS))
        {
            throw new Exception("Unknown/unhandled ORL type: " + type);
        }

        //set the data source for the dataset
        setDataSources(files);

        //import the file
        importFile(files[0], dbName, type, overwrite);

        //perform post import changes
        postImport(overwrite);

        //show import status
        if(Constants.SHOW_STATUS)
            System.out.println("Finished importing dataset type \"" + type + "\""
                    + " at " + java.util.Calendar.getInstance().getTime());
    }

    /**
     * import a single file into the specified database
     * @param file - the file to be ingested in
     * @param dbName - the database into which the data is ingested from the file
     * @param details - the details with which to import the file
     */
    public void importFile(File file, String dbName, String datasetType, boolean overwrite) throws Exception
    {
        // get a bufferedreader for the file to be imported in
        BufferedReader reader = new BufferedReader(new FileReader(file));
        //mark file read ahead limit from beginning so we can come back
        reader.mark((int)file.length() + 1);
        //read the header commands
        initializeHeaders();
        readHeader(file, reader);
        checkHeaders(file.getAbsolutePath());

        //go back to the file beginning
        reader.reset();

        //set dataset variables not specified in files
        final TemporalResolution resolution;
        List units = new ArrayList();
        final String unitsValue = "short tons";
        final String annualUnits = unitsValue + "/year";
        final String averageDailyUnits = unitsValue + "/day";
        if(annualNotAverageDaily)
        {
            units.add(annualUnits);
            resolution = TemporalResolution.ANNUAL;
        }
        else
        {
            units.add(averageDailyUnits);
            resolution = TemporalResolution.DAILY;
        }
        dataset.setUnits(units);
        dataset.setTemporalResolution(resolution.getName());
        //dataset.setPollutants(null);

        //show import status
        if(Constants.SHOW_STATUS && comments.size() > 0)
            System.out.println("Ignored " + comments.size()
                    + " commented out lines in the file");
    }

    private void initializeHeaders()
    {
        toxicsCommandRead = false;
        extendedFormat = false;
        fileType = null;
        countryName = null;
        dataYear = null;
        dataDescriptions = new ArrayList/*<String>*/();
        comments = new ArrayList/*<String>*/();
    }//initializeHeaders()

    private void checkHeaders(String fileName) throws Exception
    {
        String command = null;
        if(!toxicsCommandRead)
        {
            command = TOXICS_COMMAND;
        }
        else if(fileType == null)
        {
            command = TYPE_COMMAND;
        }
        else if(countryName == null)
        {
            command = COUNTRY_COMMAND;
        }
        else if(dataYear == null)
        {
            command = YEAR_COMMAND;
        }

        if(command != null)
        {
            throw new Exception("Missing header command \"" + command
                    + "\" from file " + fileName);
        }
    }

    private void readHeader(File file, BufferedReader reader) throws Exception
    {
         String line = null;

         //read lines in one at a time and put the data into the database.
         while((line = reader.readLine())!= null)
         {
             //if header command
             if(line.startsWith(COMMAND))
             {
                 readHeaderLine(line);
             }
         }//while file is not empty
    }//readHeader(File, BufferedReader, FileImportDetails)

    /**
     * Take a header command line and read it.
     * @param line     - the header line to read
     * @param details  - the details with which to read the header with
     * @param acceptor - the database connection acceptor
     * @throws Exception
     */
    private void readHeaderLine(String line)
            throws Exception
    {
        String[] tokens = line.split("\\s+");
        String command = tokens[0].intern();

        //#TOXICS
        //#TOXICS NONPOINT
        //#ORL
        //#ORL NONPOINT
        if((command.equals(TOXICS_COMMAND) || command.equals(ORL_COMMAND))
        && tokens.length <= 2 && !toxicsCommandRead)
        {
            if(tokens.length == 2)
            {
                if(!dataset.getDatasetType().equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS))
                {
                    throw new Exception("\"" + command + " " + TOXICS_NONPOINT + "\" is an invalid header command for dataset type \"" + dataset.getDatasetType() + "\"");
                }

                String nonPoint = tokens[1].intern();
                if(!nonPoint.equals(TOXICS_NONPOINT))
                {
                    throw new Exception("Expected \"" + TOXICS_NONPOINT + "\" after header command \"" + command + "\" but found \"" + nonPoint + "\" instead");
                }
            }
            if(command.equals(ORL_COMMAND))
            {
                extendedFormat = true;
            }
            toxicsCommandRead = true;
        }
        //#TOXICS
        //#TOXICS NONPOINT
        //#ORL
        //#ORL NONPOINT

        //#TYPE fileType
        else if(command.equals(TYPE_COMMAND) && tokens.length > 1 && fileType == null)
        {
            //final char SPACE = '\u0020';
            fileType = line.substring(TYPE_COMMAND.length()).trim();
            checkDatasetType(dataset.getDatasetType(), fileType);
        }//#TYPE fileType

        //#COUNTRY countryName
        else if(command.equals(COUNTRY_COMMAND) && tokens.length == 2 && countryName == null)
        {
            countryName = tokens[1];
            dataset.setRegion(countryName);
        }//#COUNTRY countryName

        //#YEAR dataYear
        else if(command.equals(YEAR_COMMAND) && tokens.length == 2 && dataYear == null)
        {
            dataYear = tokens[1];
            int year = Integer.parseInt(dataYear);
            dataset.setYear(year);

            //get localized DateFormat and corresponding Calendar
            DateFormat dateFormat = DateFormat.getDateInstance();
            Calendar calendar = dateFormat.getCalendar();
            calendar.set(Calendar.YEAR, year);

            //start date (January 1, Year Midnight)
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            dataset.setStartDateTime(calendar.getTime());

            //stop date (December 31, Year one millisecond before Midnight)
            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
            calendar.set(Calendar.DAY_OF_MONTH, 31);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            dataset.setStopDateTime(calendar.getTime());
        }//#YEAR dataYear

        //#DESC description
        else if(command.equals(DESCRIPTION_COMMAND) && tokens.length > 1)
        {
            String description = line.substring(DESCRIPTION_COMMAND.length()).trim();
            //multiple description lines are allowed: add to List for later
            dataDescriptions.add(description);
        }//#DESC description

        else
        {
            //This is a comment. Add to list and ignore.
            comments.add(line);
        }
    }//readHeaderLine(String, FileImportDetails)

    protected void writeKickOutHeaders(PrintWriter writer)
    {
        //#ORL
        //#ORL NONPOINT
        //#TOXICS
        //#TOXICS NONPOINT
        if(extendedFormat)
        {
            writer.print(ORL_COMMAND);
        }
        else
        {
            writer.print(TOXICS_COMMAND);
        }
        if(dataset.getDatasetType().equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS))
        {
            writer.print(" " + TOXICS_NONPOINT);
        }
        writer.println();
        //#TYPE fileType
        writer.println(TYPE_COMMAND + " " + fileType);
        //COUNTRY countryName
        writer.println(COUNTRY_COMMAND + " " + dataset.getRegion());
        //#YEAR dataYear
        writer.println(YEAR_COMMAND + " " + dataset.getYear());
        //#DESC description
        String description = dataset.getDescription();
        if(description != null && description.length() != 0)
        {
            String[] descriptions = description.split("\\n");
            for(int i = 0; i < descriptions.length; i++)
            {
                writer.println(DESCRIPTION_COMMAND + " " + descriptions[i]);
            }
        }
    }

    private void checkDatasetType(String datasetType, String fileType) throws Exception
    {
        String keyword = null;
        String fileTypeLowerCase = fileType.toLowerCase();
        if(datasetType.equals(DatasetTypes.ORL_AREA_NONROAD_TOXICS)
        && fileTypeLowerCase.indexOf("nonroad") == -1)
        {
            keyword = "Nonroad";
        }
        else if(datasetType.equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS)
             && (fileTypeLowerCase.indexOf("nonpoint") == -1) &&
                 fileTypeLowerCase.indexOf("non-point") == -1)
        {
            //must check for "Nonpoint" before check for "Point"
            keyword = "Nonpoint";
        }
        else if(datasetType.equals(DatasetTypes.ORL_MOBILE_TOXICS)
             && fileTypeLowerCase.indexOf("mobile") == -1)
        {
            keyword = "Mobile";
        }
        else if(datasetType.equals(DatasetTypes.ORL_POINT_TOXICS)
             && fileTypeLowerCase.indexOf("point") == -1)
        {
            //must check for "Nonpoint" before check for "Point"
            keyword = "Point";
        }

        if(keyword != null)
        {
            throw new Exception("File type \"" + fileType
                    + "\" must contain the word \"" + keyword
                    + "\" to be valid for dataset type \""
                    + dataset.getDatasetType() + "\"");
        }
    }

    protected void postProcess(DataAcceptor acceptor, String tableType) throws Exception
    {
        //point
        if(tableType.equals(TableTypes.ORL_POINT_TOXICS))
        {
            String[] indexColumnNames = { ORLPointDataFormat.FIPS_NAME,
                    ORLPointDataFormat.PLANT_ID_CODE_NAME,
                    ORLPointDataFormat.POINT_ID_CODE_NAME,
                    ORLPointDataFormat.STACK_ID_CODE_NAME,
                    ORLPointDataFormat.DOE_PLANT_ID_NAME,
                    ORLPointDataFormat.SOURCE_CLASSIFICATION_CODE_NAME};
            acceptor.addIndex("orl_point_key", indexColumnNames);
        }
        //nonpoint
        if(tableType.equals(TableTypes.ORL_AREA_NONPOINT_TOXICS))
        {
            String[] indexColumnNames = { ORLAreaNonpointDataFormat.FIPS_NAME,
                    ORLAreaNonpointDataFormat.SOURCE_CLASSIFICATION_CODE_NAME };
            acceptor.addIndex("orl_nonpoint_key", indexColumnNames);
        }
        //nonroad
        if(tableType.equals(TableTypes.ORL_AREA_NONROAD_TOXICS))
        {
            String[] indexColumnNames = { ORLAreaNonroadDataFormat.FIPS_NAME,
                    ORLAreaNonroadDataFormat.SOURCE_CLASSIFICATION_CODE_NAME };
            acceptor.addIndex("orl_nonroad_key", indexColumnNames);
        }
        //mobile/onroad
        if(tableType.equals(TableTypes.ORL_MOBILE_TOXICS))
        {
            String[] indexColumnNames = { ORLMobileDataFormat.FIPS_NAME,
                    ORLMobileDataFormat.SOURCE_CLASSIFICATION_CODE_NAME };
            acceptor.addIndex("orl_mobile_key", indexColumnNames);
        }

        //set the description, combining multiple lines into one String
        String description = null;
        if(dataDescriptions.size() > 0)
        {
            Iterator it = dataDescriptions.iterator();
            StringBuffer sb = new StringBuffer((String)it.next());
            while(it.hasNext())
            {
                sb.append("\n").append((String)it.next());
            }
            description = sb.toString();
        }
        dataset.setDescription(description);
    }

    /**
     * Perform post import operations on the data set most recently added
     * into the database.
     */
    private void postImport(boolean overwrite) throws Exception
    {
        //instantiate a database acceptor. the acceptor takes care of all
        //database operations. set the database name and table name to the
        //acceptor so it knows where to put the data.
        DataAcceptor acceptor = new MySQLDataAcceptor(useTransactions, Constants.USE_PREP_STATEMENT);
        acceptor.setDB(database.getName());
        //ORL table types
        String datasetType = dataset.getDatasetType();
        String[] tableTypes = DatasetTypes.getTableTypes(datasetType);
        //remove summary table
        tableTypes = removeSummaryTable(datasetType, tableTypes);
        //there is now only one table type
        String tableType = tableTypes[0];
        String tableName = (String)dataset.getDataTable(tableType);
        acceptor.setTable(tableName);
        acceptor.startAcceptingData();

        //artificially insert the FIPS data column, a five
        //character String concatenating the state and county codes
        final String FIPS_NAME = ORLDataFormat.FIPS_NAME;
        //FIPS column
        if(!extendedFormat
                && (tableType.equals(TableTypes.ORL_AREA_NONROAD_TOXICS)
                 || tableType.equals(TableTypes.ORL_MOBILE_TOXICS)))
        {
            final int FIPS_WIDTH = 5;
            final ColumnType FIPS_TYPE = ColumnType.CHAR;
            FileImportDetails fips = new FileImportDetails(FIPS_NAME);
            fips.add(FIPS_NAME);

            fips.setWidth(FIPS_NAME, String.valueOf(FIPS_WIDTH));
            fips.setType(FIPS_NAME, FIPS_TYPE.getName());

            final String STATE_CODE_NAME = "STATE";
            final int STATE_CODE_WIDTH = 2;
            final String COUNTY_CODE_NAME = "COUNTY";
            final int COUNTY_CODE_WIDTH = 3;

            //alter table
            acceptor.addColumn(FIPS_NAME, fips.getType(FIPS_NAME), COUNTY_CODE_NAME);

            //update FIPS column
            for(int stid = 0; stid < STATE_CODE_WIDTH; stid++)
            {
                //set-up parameters for SQL update
                StringBuffer stidLike = new StringBuffer("__");
                StringBuffer stidConcat = new StringBuffer("0");
                stidLike.delete(1, stidLike.length()-stid);
                stidConcat.delete(0, stid);
                for(int cyid = 0; cyid < COUNTY_CODE_WIDTH; cyid++)
                {
                    StringBuffer cyidLike = new StringBuffer("___");
                    StringBuffer cyidConcat = new StringBuffer("00");
                    cyidLike.delete(1, cyidLike.length()-cyid);
                    cyidConcat.delete(0, cyid);
                    String[] concatExprs = {"'" + stidConcat + "'", STATE_CODE_NAME, "'" + cyidConcat + "'", COUNTY_CODE_NAME};
                    String concatExpr = acceptor.generateConcatExpr(concatExprs);
                    String[] whereColumns = {STATE_CODE_NAME, COUNTY_CODE_NAME};
                    String[] likeClauses = {stidLike.toString(), cyidLike.toString()};

                    //update
                    acceptor.updateWhereLike(FIPS_NAME, concatExpr, whereColumns, likeClauses);
                }
            }
        }

        //artificially insert the STATE data column, a four
        //character String from the reference.fips table
        final String STATE_NAME = "state_abbr";
        final int STATE_WIDTH = 4;
        final ColumnType STATE_TYPE = ColumnType.CHAR;
        FileImportDetails state = new FileImportDetails(STATE_NAME);
        state.add(STATE_NAME);

        state.setWidth(STATE_NAME, String.valueOf(STATE_WIDTH));
        state.setType(STATE_NAME, STATE_TYPE.getName());

        //STATE column
        acceptor.addColumn(STATE_NAME, state.getType(STATE_NAME), FIPS_NAME);

        //update STATE column
        /**
         * the algorithm used below is more complex than letting SQL do
         * a subquery joining the table and reference.fips table. Since
         * the tables are so huge the inner join is a time bottleneck.
         * By figuring out all the possible state abbreviations in
         * reference.fips, all the state codes used in the current table,
         * and then issuing an update for each one, our speed improvement
         * is several orders of magnitude, i.e. 2-3 mins max versus dozens
         * of minutes and even hours.
         */
        String referenceName = database.getName();
        final String FIPS_TABLE_NAME = "fips";
        final String[] fipsSelectColumns = {
                "DISTINCT " + TableModifier.STATE_COL,
                "country_code",
                "FLOOR(" + TableModifier.FIPS_COL + "/1000) state_code" };
        //select state abbreviations, country codes and state codes from reference.fips table
        ResultSet results = acceptor.select(fipsSelectColumns, referenceName, FIPS_TABLE_NAME);
        //use results to create double level map
        //first level -> country code to code-abbreviation map
        Map countryToStateCodeMap = new HashMap();
        while(results.next())
        {
            String state_abbr = results.getString(1);
            String country_code = results.getString(2);
            String state_code = results.getString(3);
            //second level -> state code to state abbreviation
            Map stateCodeToStateAbbrMap = (Map)countryToStateCodeMap.get(country_code);
            if(stateCodeToStateAbbrMap == null)
            {
                stateCodeToStateAbbrMap = new HashMap();
                countryToStateCodeMap.put(country_code, stateCodeToStateAbbrMap);
            }
            if(stateCodeToStateAbbrMap.put(state_code, state_abbr) != null)
            {
                throw new Exception("Duplicate state code '" + state_code + "' in country '" + country_code + "' for table " + FIPS_TABLE_NAME);
            }
        }//while(results.next())
        final String[] usedStateCodesSelectColumns = {"DISTINCT FLOOR(" + ORLDataFormat.FIPS_NAME + "/1000) state_code"};
        results = acceptor.select(usedStateCodesSelectColumns, database.getName(), tableName);
        //we only need to issue SQL update commands for used state codes
        List usedStateCodes = new ArrayList();
        while(results.next())
        {
            //due to the nature of ResultSet, we must iterate through
            //all the rows and extract the information before we can
            //issue another SQL command.
            usedStateCodes.add(results.getString(1));
        }//while(results.next())
        Map stateCodeToStateAbbrMap = (Map)countryToStateCodeMap.get(dataset.getRegion());
        Iterator it = usedStateCodes.iterator();
        while(it.hasNext())
        {
            String stateCode = (String)it.next();
            String stateAbbr = (String)stateCodeToStateAbbrMap.get(stateCode);
            String[] whereColumns = {"FLOOR("+ORLDataFormat.FIPS_NAME+"/1000)"};
            String[] equalsClauses = {stateCode};

            //update
            acceptor.updateWhereEquals(STATE_NAME, "'" + stateAbbr + "'", whereColumns, equalsClauses);
        }//while(it.hasNext())

        //finish
        acceptor.finishAcceptingData();

        //create the summary table
        String summaryTableType = DatasetTypes.getSummaryTableType(datasetType);
        String summaryTable = (String)dataset.getDataTables().get(summaryTableType);
        TableModifier.createORLSummaryTable(database, datasetType, (String)dataset.getDataTables().get(tableType), summaryTable, overwrite, annualNotAverageDaily);
    }

}
