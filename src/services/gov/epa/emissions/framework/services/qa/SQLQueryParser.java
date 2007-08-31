package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.framework.services.qa.QAVersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.hibernate.Session;

public class SQLQueryParser {

    // Made extensive changes to this class to handle multiple versioned datasets.

    private QAStep qaStep;

    private String tableName;

    private String emissionDatasourceName;

    private EmfDataset dataset;

    private Version version;

    private String aliasValue;

    private HibernateSessionFactory sessionFactory;

    private Hashtable<String, String[]> tableValuesAliasesVersions;

    private static final String startQueryTag = "$TABLE[";

    // Created two new tags to search:
    // 1. $DATASET_TABLE[ datasetname, tablenum ], uses default version for dataset.
    // 2. $DATASET_TABLE_VERSION[ datasetname, tablenum, versionnum ].

    private static final String startSecondQueryTag = "$DATASET_TABLE[";

    private static final String startSecondVersQueryTag = "$DATASET_TABLE_VERSION[";
    
    private static final String startQAstepQueryTag = "$DATASET_QASTEP[";
    
    private static final String startQAstepVersQueryTag = "$DATASET_QASTEP_VERSION[";

    private static final String endQueryTag = "]";

    private static final String isErrorQueryTag = "$";
    
    private QAServiceImpl qaServiceImpl;

    public SQLQueryParser(QAStep qaStep, String tableName, String emissionDatasourceName, EmfDataset dataset,
            Version version, HibernateSessionFactory sessionFactory) {
        this.qaStep = qaStep;
        this.tableName = tableName;
        this.emissionDatasourceName = emissionDatasourceName;
        this.dataset = dataset;
        this.version = version;
        this.sessionFactory = sessionFactory;
        tableValuesAliasesVersions = new Hashtable<String, String[]>();
        qaServiceImpl = new QAServiceImpl(sessionFactory);
    }

    // New constructor for far fewer arguments.
    public SQLQueryParser(HibernateSessionFactory sessionFactory, String emissionDatasourceName, String tableName) {
        this.sessionFactory = sessionFactory; 
        this.emissionDatasourceName = emissionDatasourceName;
        this.tableName = tableName;
        tableValuesAliasesVersions = new Hashtable<String, String[]>();
        qaServiceImpl = new QAServiceImpl(sessionFactory);
    }

    public String parse() throws EmfException {
        return createTableQuery() + userQuery(qaStep.getProgramArguments());
    }

    public String parse(String inputQuery, boolean createClause) throws EmfException {
        if (createClause == true) {
            return createTableQuery() + userQuery(inputQuery);
        }

        return userQuery(inputQuery);
    }

    private String userQuery(String query) throws EmfException {
        // This is an indication that the startQueryString tag was not found in the query.

        if (query.indexOf(startQueryTag) == -1 && query.indexOf(startSecondQueryTag) == -1
                && query.indexOf(startSecondVersQueryTag) == -1 && query.indexOf(startQAstepQueryTag) == -1
                && query.indexOf(startQAstepVersQueryTag) == -1)
            return query;

        return expandTag(query);
    }

    // SELECT - REQUIRED to STARTS WITH
    // FROM - REQUIRED
    // WHERE - OPTIONAL
    // ASSUME table is emissions datasource
    // ASSUME table is versioned

    private String expandTag(String query) throws EmfException {

        while ((query.indexOf(startQueryTag)) != -1) {
            query = expandOneTag(query);
        }
        // System.out.println("The query at point 1 is " + query);
        // Added two new while clauses to handle the parsing of the two new tags
        // Added code to add a new tag
        while ((query.indexOf(startSecondQueryTag)) != -1) {
            query = expandTwoTag(query);
        }
        // System.out.println("The query at point 2 is " + query);
        // Added code to add a new tag
        while ((query.indexOf(startSecondVersQueryTag)) != -1) {
            query = expandThreeTag(query);
        }
        //System.out.println("The query at point 3 is " + query);
        while ((query.indexOf(startQAstepQueryTag)) != -1) {
            query = expandFourTag(query);
        }
        
        while ((query.indexOf(startQAstepVersQueryTag)) != -1) {
            query = expandFiveTag(query);
        }

        // Check to see if there are any "$'s" left
        if (query.indexOf(isErrorQueryTag) != -1) {
            throw new EmfException("There is still a  '" + isErrorQueryTag + "' in the program arguments");
        }
        // System.out.println("The query at point 3 is " + query);
        return versioned(query);
    }

    private String expandOneTag(String query) throws EmfException {

        // Point to the start of the first tag, the first substring is from the beginning of the
        // query to that point. The suffix is from that point to the end of the tag.
        int index = query.indexOf(startQueryTag);
        System.out.println("orig: " + query + " uper: " + query + " startQT:" + startQueryTag);
        String prefix = query.substring(0, index);
        String suffix = query.substring(index + startQueryTag.length());

        // Split the suffix into three tokens
        String[] suffixTokens = suffixSplit(suffix);

        // The dataset name is retreived from the attribute, as is the version for it.
        // The version has to be further subdivided to just get the number.

        String dataSetName = dataset.toString();
        String ds1version = version.toString();
        ds1version = ds1version.substring(0, 1);

        // The dataset id is retrieved from the dataset through its getId method.
        // It is then converted to a string to put in the hashtable.

        int dataSet_id = dataset.getId();
        String dsId = Integer.toString(dataSet_id);

        /*
         * System.out.println("dataset of 1: " + dataSetName); System.out.println("version of 1: " + ds1version);
         * System.out.println("alias of 1: " + suffixTokens[0]); System.out.println("Dataset id: " + dataSet_id);
         */

        // The complete path of the version is retrieved from the version attribute.
        String versionCompletePath = version.createCompletePath();

        // The first string array value is calculated for the hashtable.
        // The appropriate key plus the string array (as a "value") is put into the hashtable.

        String[] ds1 = { dataSetName, suffixTokens[0], ds1version, dsId, versionCompletePath };
        tableValuesAliasesVersions.put("ds1", ds1);

        // The table name from the dataset is derived from the method below off of the second token.
        return prefix + tableNameFromDataset(suffixTokens[1], dataset) + suffixTokens[2];
    }

    // Added this method to handle new tag $DATASET_TABLE[ datasetname, tablenum ]

    private String expandTwoTag(String query) throws EmfException {
        // Point to the start of the second tag, the first substring is from the beginning of the
        // query to that point. The suffix is from that point to the end of the tag.

        int index = query.indexOf(startSecondQueryTag);
        //System.out.println(query + "[]" + query);
        String prefix = query.substring(0, index);
        String suffix = query.substring(index + startSecondQueryTag.length());

        // Split the suffix into three tokens
        String[] suffixTokens = suffixSplit(suffix);
        //for (int v = 0; v < suffixTokens.length; v++) {
          //  System.out.println(suffixTokens[v]+ "\n");
            //}

        // The dataset name is retreived as the first argument in the second token.
        // It is then trimmed and then converted into an EmfDataset type throught
        // the getDataset method created below

        int index2 = suffixTokens[1].indexOf(",");
        String dataSetName = suffixTokens[1].substring(0, index2) + " ";
        EmfDataset dataSet2 = getDataset(dataSetName.trim());

        // The integer value of the default version is retrieved from the getDefaultVersion
        // method of the EmfDataset just created. It is converted to a String for the hashtable.

        int ds2IntVersion = dataSet2.getDefaultVersion();
        String ds2version = Integer.toString(ds2IntVersion);

        // System.out.println("Default Version as integer " + ds2IntVersion);

        // The dataset id is retrieved from the dataset through its getId method.
        // It is then converted to a string to put in the hashtable.
        // Next a version object is created from the dataset id and the version.

        int dataSet2_id = dataSet2.getId();
        String ds2Id = Integer.toString(dataSet2_id);
        Version version1 = version(dataSet2_id, ds2IntVersion);
        // System.out.println("version object" + version1);

        // The table number is retrieved from the second argument in the second token.
        String tableNum = suffixTokens[1].substring(index2 + 1).trim();

        /*
         * System.out.println("Dataset information: " + dataSet2); System.out.println("Dataset id: " + dataSet2_id);
         * System.out.println("Dataset name: " + dataSetName); System.out.println("Table number: " + tableNum);
         */

        // The complete path of the version is retrieved from the version object just created.
        String versionCompletePath = version1.createCompletePath();

        // The second string array value is calculated for the hashtable.
        // The appropriate key plus the string array (as a "value") is put into the hashtable.
        String[] ds2 = { dataSetName, suffixTokens[0], ds2version, ds2Id, versionCompletePath };
        tableValuesAliasesVersions.put("ds2", ds2);

        /*
         * System.out.println("prefix: " + prefix); System.out.println("Table number: " + tableNum);
         * System.out.println("dataset of 2: " + dataSetName); System.out.println("version of 2: " + ds2version);
         * System.out.println("alias of 2: " + suffixTokens[0]); System.out.println("Rest of query: " +
         * suffixTokens[2]);
         */

        // The table name from the dataset is derived from the method below off of the second token.
        return prefix + tableNameFromDataset(tableNum, dataSet2) + suffixTokens[2];
    }

    // Added this method to handle new tag $DATASET_TABLE_VERSION[ datasetname, tablenum, versionnum ]

    private String expandThreeTag(String query) throws EmfException {

        String versionCompletePath;

        // Point to the start of the third tag, the first substring is from the beginning of the
        // query to that point. The suffix is from that point to the end of the tag.
        int index = query.indexOf(startSecondVersQueryTag);
        String prefix = query.substring(0, index);
        String suffix = query.substring(index + startSecondVersQueryTag.length());

        // Split the suffix into three tokens
        String[] suffixTokens = suffixSplit(suffix);

        // The dataset name is retreived as the first argument in the second token.
        StringTokenizer tokenizer = new StringTokenizer(suffixTokens[1], ",");
        // int index2 = suffixTokens[1].indexOf(",");
        String dataSetName = tokenizer.nextToken();
        String tableNum = tokenizer.nextToken().trim();
        String ds3version = tokenizer.nextToken().trim();

        // The (String) value of the version is retrieved as the third argument
        // of the tag. It is then converted to an integer. The value is then compared
        // to make sure it is > or = 0.

        // String ds3version = suffixTokens[1].substring(index2 + 5).trim();
        // String ds3version = suffixTokens[1].substring(index2 + 5);
        // System.out.println("This is not a number " + ds3version);
        int ds3IntVersion = Integer.parseInt(ds3version);
        // System.out.println("This is a number " + ds3IntVersion);
        if (ds3IntVersion < 0)
            throw new EmfException("The version number should be greater or equal to zero");

        // The dataset name is trimmed and then converted into an EmfDataset type through
        // the getDataset method.
        EmfDataset dataSet3 = getDataset(dataSetName.trim());

        // The dataset id is retrieved from the dataset through its getId method.
        // It is then converted to a string to put in the hashtable.
        // Then the version is converted to an integer value.

        int dataSet3_id = dataSet3.getId();
        String ds3Id = Integer.toString(dataSet3_id);

        // The table number is retrieved from the second argument in the second token.
        // String tableNum = suffixTokens[1].substring(index2 + 1, index2 + 3).trim();

        // System.out.println("The integer version is: " + ds3IntVersion);
        // System.out.println("Version as integer " + ds3IntVersion);

        // A version object is created from the dataset id and the version.
        Version version1 = version(dataSet3_id, ds3IntVersion);

        // Chcek to see if the version1 value is valid, if not throw an exception.
        if (version1 == null)
            throw new EmfException("The version name must be valid");
        // System.out.println("version object " + version1);

        // The complete path of the version is retrieved from the version object just created.

        versionCompletePath = version1.createCompletePath();

        // For debugging purposes

        /*
         * System.out.println("prefix: " + prefix); System.out.println("Table number: " + tableNum);
         * System.out.println("Dataset information: " + dataSet3); System.out.println("Dataset id: " + dataSet3_id);
         * //System.out.println("Dataset name: " + dataSetName); System.out.println("dataset of 3: " + dataSetName);
         * //System.out.println("Table number: " + tableNum); System.out.println("version of 3: " + ds3version);
         * System.out.println("alias of 3: " + suffixTokens[0]); System.out.println("The complete path for version 3 is: " +
         * versionCompletePath);
         */

        // The third string array value is calculated for the hashtable.
        // The appropriate key plus the string array (as a "value") is put into the hashtable.
        String[] ds3 = { dataSetName, suffixTokens[0], ds3version, ds3Id, versionCompletePath };
        tableValuesAliasesVersions.put("ds3", ds3);

        // The table name from the dataset is derived from the method below off of the second token.
        return prefix + tableNameFromDataset(tableNum, dataSet3) + suffixTokens[2];
        // return "OK";
    }
    
//  Added this method to handle new tag $DATASET_QASTEP[ datasetname, QAStepName ]

    private String expandFourTag(String query) throws EmfException {
        // Point to the start of the second tag, the first substring is from the beginning of the
        // query to that point. The suffix is from that point to the end of the tag.
        String outputTable = "";
        
        int index = query.indexOf(startQAstepQueryTag);
        
        String prefix = query.substring(0, index);
        //System.out.println("Prefix: "+ prefix);
        String suffix = query.substring(index + startQAstepQueryTag.length());
        //System.out.println("Suffix: "+ suffix);

        // Split the suffix into two tokens
        String[] suffixTokens = noAliasSuffixSplit(suffix);

        // The dataset name is retreived as the first argument in the second token.
        // It is then trimmed and then converted into an EmfDataset type throught
        // the getDataset method created below

        int index4 = suffixTokens[0].indexOf(",");
        String dataSetName = suffixTokens[0].substring(0, index4) + " ";
        EmfDataset dataSet4 = getDataset(dataSetName.trim());
        //System.out.println("Database name = \n" + dataSet4 + "\n");

        // The integer value of the default version is retrieved from the getDefaultVersion
        // method of the EmfDataset just created. It is converted to a String for the hashtable.

        int ds4IntVersion = dataSet4.getDefaultVersion();

        //System.out.println("Default Version as integer " + ds4IntVersion);

        // The dataset id is retrieved from the dataset through its getId method.
        // It is then converted to a string to put in the hashtable.
        // Next a version object is created from the dataset id and the version.

        //int dataSet4_id = dataSet4.getId();
        //String ds4Id = Integer.toString(dataSet4_id);
        //Version version1 = version(dataSet4_id, ds4IntVersion);
        //System.out.println("version object" + version1);

        //The QA Step name is retrieved from the second argument in the second token.
        String qaStepName = suffixTokens[0].substring(index4 + 1).trim();
        //System.out.println("QA Step Name: " + qaStepName);
        
        // Use the QAServiceImpl object which was created in the constructor.  Run the 
        // method which gets an array of the QA Steps associated with this dataset.
        
        QAStep [] qaSteps = qaServiceImpl.getQASteps(dataSet4);
        //System.out.println("The array size is: " + qaSteps.length);
        
        // Go through each of them and find the matching dataset with the correct name and
        // version number.
        for (int r = 0; r < qaSteps.length; r++) {
            //System.out.println("Next step: " + qaSteps[r]);
            //System.out.println(" Step version : " + qaSteps[r].getVersion());
            if (qaSteps[r].toString().equals(qaStepName) && qaSteps[r].getVersion()== ds4IntVersion) {
                QAStepResult qaStepResult = qaServiceImpl.getQAStepResult(qaSteps[r]);
                
                outputTable = qaStepResult.getTable();
            //System.out.println("The output table for the next step: " + outputTable);
            }
        }
        if (outputTable == "")
                throw new EmfException("An appropriate QA Step could not be found");
       
        // Only need to send back the schema concatenated with the output table.
        return prefix + emissionDatasourceName + "." + outputTable + suffixTokens[1];
    }

    // Added method to handle new tag $DATASET_QASTEP_VERSION[ datasetname, QAStepName, versionnum]
    
    private String expandFiveTag(String query) throws EmfException {
        // Point to the start of the second tag, the first substring is from the beginning of the
        // query to that point. The suffix is from that point to the end of the tag.
        String outputTable = "";
        
        int index = query.indexOf(startQAstepVersQueryTag);
        
        String prefix = query.substring(0, index);
        //System.out.println("Prefix: "+ prefix);
        String suffix = query.substring(index + startQAstepVersQueryTag.length());
        //System.out.println("Suffix: "+ suffix);

        // Split the suffix into two tokens
        String[] suffixTokens = noAliasSuffixSplit(suffix);

        // The dataset name is retreived as the first argument in the second token.
        // It is then trimmed and then converted into an EmfDataset type throught
        // the getDataset method created below.
        
        // The dataset name is retreived as the first argument in the first token.
        StringTokenizer tokenizer = new StringTokenizer(suffixTokens[0], ",");
        // int index2 = suffixTokens[1].indexOf(",");
        String dataSetName = tokenizer.nextToken();
        String qaStepName = tokenizer.nextToken().trim();
        String ds5version = tokenizer.nextToken().trim();

        //int index5 = suffixTokens[0].indexOf(",");
        //String dataSetName = suffixTokens[0].substring(0, index5) + " ";
        EmfDataset dataSet5 = getDataset(dataSetName.trim());
        //System.out.println("Database name = \n" + dataSet4 + "\n");

        // The integer value of the default version is retrieved from the getDefaultVersion
        // method of the EmfDataset just created. It is converted to a String for the hashtable.

        //int ds5IntVersion = dataSet5.getDefaultVersion();

        //System.out.println("Default Version as integer " + ds4IntVersion);

        // The dataset id is retrieved from the dataset through its getId method.
        // It is then converted to a string to put in the hashtable.
        // Next a version object is created from the dataset id and the version.
        
        // The (String) value of the version is retrieved as the third argument
        // of the tag. It is then converted to an integer. The value is then compared
        // to make sure it is > or = 0.

        int ds5IntVersion = Integer.parseInt(ds5version);
        //System.out.println("This is a number " + ds5IntVersion);
        if (ds5IntVersion < 0)
            throw new EmfException("The version number should be greater or equal to zero");

        //int dataSet5_id = dataSet5.getId();
        //String ds5Id = Integer.toString(dataSet5_id);
        //Version version1 = version(dataSet5_id, ds5IntVersion);
        //System.out.println("version object" + version1);
        //System.out.println("version as integer" + ds5Id);

        //System.out.println("QA Step Name: " + qaStepName);
        
        // Use the QAServiceImpl object which was created in the constructor.  Run the 
        // method which gets an array of the QA Steps associated with this dataset.
        
        QAStep [] qaSteps = qaServiceImpl.getQASteps(dataSet5);
        //System.out.println("The array size is: " + qaSteps.length);
        
        // Go through each of them and find the matching dataset with the correct name and
        // version number.
        for (int r = 0; r < qaSteps.length; r++) {
            //System.out.println("The next step: " + qaSteps[r]);
            //int qaName = qaSteps[r].getId();
            //System.out.println("The id of the next step: " + qaName);
            if (qaSteps[r].toString().equals(qaStepName) && qaSteps[r].getVersion()== ds5IntVersion) {
                QAStepResult qaStepResult = qaServiceImpl.getQAStepResult(qaSteps[r]);
            outputTable = qaStepResult.getTable();
            //System.out.println("The output table for the next step: " + outputTable);
            }
        }
        if (outputTable == "")
            throw new EmfException("An appropriate QA Step could not be found");
        
        // Only need to send back the schema concatenated with the output table.
        return prefix + emissionDatasourceName + "." + outputTable + suffixTokens[1];
    }


    // Added method to create new version objects associated with the second and third tags.
    private Version version(int dataset_id, int versionNum) {
        // System.out.println("OK");
        Session session = sessionFactory.getSession();
        try {
            // System.out.println("OK");
            return new Versions().get(dataset_id, versionNum, session);

        } finally {
            session.close();
        }
    }

    private String versioned(String partQuery) {
        String versionClause = versionClause();
        // For debugging purposes
        // System.out.println("The version clause part of the query is " + versionClause);
        return insertVersionClause(partQuery, versionClause);

    }

    private String insertVersionClause(String partQuery, String versionClause) {
        
        String[] keywords = { "GROUP BY", "HAVING", "ORDER BY", "LIMIT" };
        //System.out.println("Version clause: " + versionClause);
        //Make new string of upper case just to handle these keywords.
        String tempPartQuery = partQuery.toUpperCase();
        //Create a "tempQuery to upper case" here
        String firstPart = partQuery;
        String secondPart = "";
        for (int i = 0; i < keywords.length; i++) {
            int index = tempPartQuery.indexOf(keywords[i]);
            if (index != -1) {
                firstPart = partQuery.substring(0, index);
                secondPart = partQuery.substring(index);
                break;
            }
        }
        if (tempPartQuery.indexOf("WHERE") == -1) {
        //if (firstPart.indexOf("WHERE") == -1)
            if (versionClause == "")
                return firstPart +  versionClause + " " + secondPart;
            
            return firstPart + " WHERE " + versionClause + " " + secondPart;
        }
        return firstPart + " AND " + versionClause + " " + secondPart;
    }

    // Modified method below to use an object of class QAVersioned Query instead of an object of class
    // VersionedQuery.

    private String versionClause() {
        QAVersionedQuery query = new QAVersionedQuery();

        // Modified to pass the hashtable instead of a scalar.
        return query.query(tableValuesAliasesVersions);
    }

    // Modified method below to add the alias value for multiple tables.

    private String[] suffixSplit(String token) throws EmfException {

        String nextDollarSign;

        // Look for a missing end tag and either the end of the query or the next dollar sign
        // If it is missing, throw an exception.

        if (token.indexOf(isErrorQueryTag) != -1)
            nextDollarSign = token.substring(0, token.indexOf(isErrorQueryTag));
        else
            nextDollarSign = token;

        int index = nextDollarSign.indexOf(endQueryTag);
        if (index == -1)
            throw new EmfException("The '" + endQueryTag + "' is expected in the program arguments");

        // Break up the token into a prefix and suffix string.

        String prefix = token.substring(0, index);
        String suffix = token.substring(index + endQueryTag.length());

        /*
         * System.out.println("suffixSplit prefix: " + prefix); System.out.println("suffixSplit suffix: " + suffix);
         * System.out.println("Index: " + index);
         */

        // Added to find alias value
        /*
         * System.out.println("Should be a space: " + suffix.substring(0,1)); System.out.println("Should be a letter: " +
         * suffix.substring(1,2)); System.out.println("Should also be a space: " + suffix.substring(2,3));
         */

        // Determine whether the alias value exists or not. If it does, isolate it.
        // If it does not, throw an exception.
        if (suffix.charAt(0) == ' ' && suffix.charAt(1) != ',' && (suffix.charAt(2) == ' ' || suffix.charAt(2) == ',')) {
            aliasValue = suffix.substring(0, 2);
            aliasValue = aliasValue.trim();
        } else {
            throw new EmfException("A one-character alias value is expected after the '" + endQueryTag + "'");
        }

        return new String[] { aliasValue, prefix, suffix };
    }
    
    
    private String[] noAliasSuffixSplit(String token) throws EmfException {

        String nextDollarSign;

        // Look for a missing end tag and either the end of the query or the next dollar sign
        // If it is missing, throw an exception.

        if (token.indexOf(isErrorQueryTag) != -1)
            nextDollarSign = token.substring(0, token.indexOf(isErrorQueryTag));
        else
            nextDollarSign = token;

        int index = nextDollarSign.indexOf(endQueryTag);
        if (index == -1)
            throw new EmfException("The '" + endQueryTag + "' is expected in the program arguments");

        // Break up the token into a prefix and suffix string.

        String prefix = token.substring(0, index);
        String suffix = token.substring(index + endQueryTag.length());
        
        return new String[] {prefix, suffix };
    }


    private String tableNameFromDataset(String tableNo, EmfDataset dataset) throws EmfException {
        int tableID = tableID(tableNo);
        InternalSource[] internalSources = dataset.getInternalSources();
        if (internalSources.length < tableID)
            throw new EmfException("The table number is larger than the number of tables in the dataset");
        return qualifiedName(emissionDatasourceName, internalSources[tableID - 1].getTable());
    }

    private int tableID(String tableNo) throws EmfException {
        try {
            int value = Integer.parseInt(tableNo);
            if (value <= 0)
                throw new EmfException("The table number should be greater or equal to one");
            return value;
        } catch (NumberFormatException e) {
            throw new EmfException("Could not convert the table number to an integer -" + qaStep.getProgramArguments());
        }
    }

    private String createTableQuery() {
        // System.out.println("The table name in createTableQuery method is: " + tableName);
        return "CREATE TABLE " + qualifiedName(emissionDatasourceName, tableName) + " AS ";
    }

    private String qualifiedName(String datasourceName, String tableName) {
        return datasourceName + "." + tableName;
    }

    // Added method to get the dataset given the dataset name.

    private EmfDataset getDataset(String dsName) throws EmfException {
        //System.out.println("Database name = \n" + dsName + "\n");
        DatasetDAO dao = new DatasetDAO();
        try {
            return dao.getDataset(sessionFactory.getSession(), dsName);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("The dataset name must be valid");
        }
    }

}
