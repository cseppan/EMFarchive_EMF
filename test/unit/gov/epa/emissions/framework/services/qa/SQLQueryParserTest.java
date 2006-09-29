package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import java.sql.SQLException;

import org.jmock.MockObjectTestCase;

public class SQLQueryParserTest extends MockObjectTestCase {

    private String emissioDatasourceName;

    public SQLQueryParserTest() {
        emissioDatasourceName = "emissions";
    }

    protected void addRecord(Datasource datasource, String table, String[] data) throws SQLException {
        DataModifier modifier = datasource.dataModifier();
        modifier.insertRow(table, data);
    }

    public void testShoudParseTheQueryWhichDoesNotContailTags() throws Exception {
        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        String userQuery = "SELECT * FROM reference.pollutants";
        qaStep.setProgramArguments(userQuery);

        SQLQueryParser parser = new SQLQueryParser(qaStep, "table1", emissioDatasourceName, null, null);
        String query = parser.parse();
        String expected = "CREATE TABLE emissions.table1 AS " + userQuery.toUpperCase();
        
        assertEquals(expected, query);
    }

    public void testShoudParseTheQueryWhichDoesContainsSimpleTag() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE[1]";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version);
        String query = parser.parse();
        String expected = "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT * FROM "
                + qualfiedName(tableName)
                + " WHERE version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
                + dataset.getId() + " ";

        assertEquals(expected.toUpperCase(), query.toUpperCase());
    }

    public void testShoudThrowAnException_TableNoIsMoreThanExistingNumberOfTables() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE[2]";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version);
        try {
            parser.parse();
            assertTrue("Should have throw an exception", false);
        } catch (EmfException e) {
            assertEquals("The table number is more than the number tables for the dataset", e.getMessage());
        }
    }

    public void testShouldExpandTwoTagsOnTheQuery() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE[1] WHERE $TABLE[1].scc=reference.scc.scc";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version);
        String query = parser.parse();
        String expected = "CREATE TABLE "
                + qualfiedName(qaStepOutputTable)
                + " AS SELECT * FROM "
                + qualfiedName(tableName)
                + " WHERE emissions.table1.scc=reference.scc.scc AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
                + dataset.getId() + " ";

        assertEquals(expected.toUpperCase(), query.toUpperCase());

    }

public void testShouldExpandWithWHEREClause() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE[1] WHERE poll='NOX'";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version);
        String query = parser.parse();
        String expected = "CREATE TABLE "
                        + qualfiedName(qaStepOutputTable)
                        + " AS SELECT * FROM "
                        + qualfiedName(tableName)
                        + " WHERE poll='NOX' AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
                        + dataset.getId()+" ";
        assertEquals(expected.toUpperCase(), query.toUpperCase());

    }    public void testShouldExpandWithGROUPBYClause() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT scc,sum(ANN_EMIS) FROM $TABLE[1] GROUP BY scc";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version);
        String query = parser.parse();

        String expected = "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT scc,sum(ANN_EMIS) FROM "
                + qualfiedName(tableName)
                + "  WHERE version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
                + dataset.getId() + " GROUP BY scc";
        assertEquals(expected.toUpperCase(), query.toUpperCase());

    }

    public void testShouldExpandWithGROUPBY_AND_WHEREClause() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT scc,sum(ANN_EMIS) FROM $TABLE[1] WHERE Poll='NOx' GROUP BY scc";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version);
        String query = parser.parse();

        String expected = "CREATE TABLE "
                + qualfiedName(qaStepOutputTable)
                + " AS SELECT scc,sum(ANN_EMIS) FROM "
                + qualfiedName(tableName)
                + " WHERE Poll='NOx'  AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
                + dataset.getId() + " GROUP BY scc";
        assertEquals(expected.toUpperCase(), query.toUpperCase());

    }

    public void testShouldExpandWithORDERBY_AND_WHEREClause() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE[1] WHERE Poll='NOx' ORDER BY ANN_EMIS";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version);
        String query = parser.parse();

        String expected = "CREATE TABLE "
                + qualfiedName(qaStepOutputTable)
                + " AS SELECT * FROM "
                + qualfiedName(tableName)
                + " WHERE Poll='NOx'  AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
                + dataset.getId() + " ORDER BY ANN_EMIS";
        assertEquals(expected.toUpperCase(), query.toUpperCase());

    }

    public void testShouldExpandWithGROUPBY_ORDERBY_LIMIT_AND_WHEREClause() throws Exception {
        String tableName = "table1";
        int datasetId = 0;
        EmfDataset dataset = dataset(datasetId, tableName);
        Version version = version(datasetId);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT SCC,SUM(ANN_EMIS) AS SUM_EMIS FROM $TABLE[1] WHERE Poll='NOx' GROUP BY SCC ORDER BY SUM_EMIS LIMIT 5";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(qaStep, qaStepOutputTable, emissioDatasourceName, dataset, version);
        String query = parser.parse();

        String expected = "CREATE TABLE "
                + qualfiedName(qaStepOutputTable)
                + " AS SELECT SCC,SUM(ANN_EMIS) AS SUM_EMIS FROM "
                + qualfiedName(tableName)
                + " WHERE Poll='NOx'  AND version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="
                + dataset.getId() + " GROUP BY SCC ORDER BY SUM_EMIS LIMIT 5";
        assertEquals(expected.toUpperCase(), query.toUpperCase());

    }

    private String qualfiedName(String tableName) {
        return emissioDatasourceName + "." + tableName;
    }

    private EmfDataset dataset(int datasetId, String tableName) {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setId(datasetId);
        InternalSource source = new InternalSource();
        source.setTable(tableName);
        source.setSource("test");
        source.setCols(new String[] {});
        source.setType("TEST TYPE");
        dataset.setInternalSources(new InternalSource[] { source });

        return dataset;
    }

    private Version version(int datasetId) {
        Version version = new Version();
        version.setId(0);
        version.setDatasetId(datasetId);
        version.setVersion(0);
        version.setName("Initial Version");
        version.setPath("");
        return version;
    }

}
