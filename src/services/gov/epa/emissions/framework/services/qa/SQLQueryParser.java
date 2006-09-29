package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class SQLQueryParser {

    private QAStep qaStep;

    private String tableName;

    private String emissionDatasourceName;

    private EmfDataset dataset;

    private Version version;

    private static final String startQueryTag = "$TABLE[";

    private static final String endQueryTag = "]";

    public SQLQueryParser(QAStep qaStep, String tableName, String emissionDatasoureName, EmfDataset dataset,
            Version version) {
        this.qaStep = qaStep;
        this.tableName = tableName;
        this.emissionDatasourceName = emissionDatasoureName;
        this.dataset = dataset;
        this.version = version;
    }

    public String parse() throws EmfException {
        return createTableQuery() + userQuery(qaStep.getProgramArguments());
    }

    private String userQuery(String query) throws EmfException {
        query = query.toUpperCase();
        if (query.indexOf(startQueryTag) == -1)
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
        return versioned(query);
    }

    // error if end query tag is not found
    private String expandOneTag(String query) throws EmfException {
        int index = query.indexOf(startQueryTag);
        String prefix = query.substring(0, index);
        String suffix = query.substring(index + startQueryTag.length());
        String[] suffixTokens = suffixSplit(suffix);

        return prefix + tableNameFromDataset(suffixTokens[0]) + suffixTokens[1];
    }

    private String versioned(String partQuery) {
        String versionClause = versionClause();
        return insertVersionClause(partQuery, versionClause);

    }

    private String insertVersionClause(String partQuery, String versionClause) {
        String[] keywords = { "GROUP BY", "HAVING", "ORDER BY", "LIMIT" };

        String firstPart = partQuery;
        String secondPart = "";
        for (int i = 0; i < keywords.length; i++) {
            int index = partQuery.indexOf(keywords[i]);
            if (index != -1) {
                firstPart = partQuery.substring(0, index);
                secondPart = partQuery.substring(index);
                break;
            }
        }
        if (firstPart.indexOf("WHERE") == -1)
            return firstPart + " WHERE " + versionClause + " " + secondPart;

        return firstPart + " AND " + versionClause + " " + secondPart;
    }

    private String versionClause() {
        VersionedQuery query = new VersionedQuery(version);
        return query.query();
    }

    private String[] suffixSplit(String token) throws EmfException {
        int index = token.indexOf(endQueryTag);
        if (index == -1)
            throw new EmfException("The '" + endQueryTag + "' is expected in the program arguments");
        String prefix = token.substring(0, index);
        String suffix = token.substring(index + endQueryTag.length());
        return new String[] { prefix, suffix };
    }

    private String tableNameFromDataset(String tableNo) throws EmfException {
        int tableID = tableID(tableNo);
        InternalSource[] internalSources = dataset.getInternalSources();
        if (internalSources.length < tableID)
            throw new EmfException("The table number is more than the number tables for the dataset");
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
        return "CREATE TABLE " + qualifiedName(emissionDatasourceName, tableName) + " AS ";
    }

    private String qualifiedName(String datasourceName, String tableName) {
        return datasourceName + "." + tableName;
    }

}
