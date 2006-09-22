package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.hibernate.Session;

public class SQLQueryParser {

    private DbServer dbServer;

    private QAStep qaStep;

    private String tableName;

    private HibernateSessionFactory sessionFactory;

    private static final String startQueryTag = "$TABLE{";

    private static final String endQueryTag = "}";

    public SQLQueryParser(DbServer dbServer, HibernateSessionFactory sessionFactory, QAStep qaStep, String tableName) {
        this.dbServer = dbServer;
        this.sessionFactory = sessionFactory;
        this.qaStep = qaStep;
        this.tableName = tableName;
    }

    public String parse() throws EmfException {
        return createTableQuery() + userQuery(qaStep.getProgramArguments());
    }

    private String userQuery(String query) throws EmfException {
        if (query.indexOf(startQueryTag) == -1)
            return query;

        return expandTag(query);// TODO: parse the query
    }

    // ONE TAG PER STATEMENT
    // SELECT - REQUIRED to STARTS WITH
    // FROM - REQUIRED
    // WHERE - OPTIONAL
    // ASSUME table is emissions datasource
    // ASSUME table is versioned
    private String expandTag(String query) throws EmfException {
        int index = query.indexOf(startQueryTag);
        String prefix = query.substring(0, index);
        String suffix = query.substring(index + startQueryTag.length());
        // more than two tokens error?
        // error if end query tag is not found
        String[] suffixTokens = suffixSplit(suffix);

        return prefix + tableNameFromDataset(suffixTokens[0]) + versioned(suffixTokens[1]);
    }

    private String versioned(String partQuery) {
        String versionClause = versionClause();
        if (partQuery.indexOf("WHERE") == -1)
            return partQuery + " WHERE " + versionClause;

        return partQuery + " AND " + versionClause;
    }

    private String versionClause() {
        Session session = sessionFactory.getSession();
        try {
            Version version = new Versions().get(qaStep.getDatasetId(), qaStep.getVersion(), session);
            VersionedQuery query = new VersionedQuery(version);
            return query.query();
        } finally {
            session.close();
        }
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
        DatasetDAO dao = new DatasetDAO();
        Session session = sessionFactory.getSession();
        try {
            EmfDataset dataset = dao.getDataset(session, qaStep.getDatasetId());
            InternalSource[] internalSources = dataset.getInternalSources();
            if (internalSources.length < tableID - 1)
                throw new EmfException("The table number is more than number tables for the dataset");
            return qualifiedName(dbServer.getEmissionsDatasource(), internalSources[tableID - 1].getTable());
        } finally {
            session.close();
        }

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
        return "CREATE TABLE " + qualifiedName(dbServer.getEmissionsDatasource(), tableName) + " AS ";
    }

    private String qualifiedName(Datasource datasource, String tableName) {
        return datasource.getName() + "." + tableName;
    }

}
