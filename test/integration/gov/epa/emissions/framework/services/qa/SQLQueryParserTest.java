package gov.epa.emissions.framework.services.qa;

import java.sql.SQLException;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class SQLQueryParserTest extends ServiceTestCase {

    private String versionsTable;

    public SQLQueryParserTest() {
        versionsTable = "versions";
    }

    protected void doSetUp() throws Exception {
        //
    }

    protected void doTearDown() throws Exception {
        clean();
        dropAll(EmfDataset.class);
    }

    private void clean() throws SQLException {
        DataModifier modifier = dbServer().getEmissionsDatasource().dataModifier();
        modifier.dropAllData(versionsTable);
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
        SQLQueryParser parser = new SQLQueryParser(dbServer(), sessionFactory(), qaStep, "table1");
        String query = parser.parse();
        assertEquals("CREATE TABLE emissions.table1 AS " + userQuery, query);
    }

    public void testShoudParseTheQueryWhichDoesContainsSimpleTag() throws Exception {
        String tableName = "table1";
        EmfDataset dataset = addDataset(tableName);
        setupVersionZero(dbServer().getEmissionsDatasource(), dataset.getId(), versionsTable);

        QAStep qaStep = new QAStep();
        qaStep.setName("Step1");
        qaStep.setDatasetId(dataset.getId());
        String userQuery = "SELECT * FROM $TABLE{1}";
        qaStep.setProgramArguments(userQuery);
        String qaStepOutputTable = "qa_table1";
        SQLQueryParser parser = new SQLQueryParser(dbServer(), sessionFactory(), qaStep, qaStepOutputTable);
        String query = parser.parse();
        assertEquals(
                "CREATE TABLE " + qualfiedName(qaStepOutputTable) + " AS SELECT * FROM " + qualfiedName(tableName)+" WHERE version IN (0) AND  delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)' AND dataset_id="+dataset.getId(),
                query);
    }

    private String qualfiedName(String tableName) {
        return dbServer().getEmissionsDatasource().getName() + "." + tableName;
    }

    private EmfDataset addDataset(String tableName) {
        User owner = new UserDAO().get("emf", session);

        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setCreator(owner.getUsername());
        InternalSource source = new InternalSource();
        source.setTable(tableName);
        source.setSource("test");
        source.setCols(new String[] {});
        source.setType("TEST TYPE");
        dataset.setInternalSources(new InternalSource[] { source });

        save(dataset);
        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

    private void setupVersionZero(Datasource datasource, int datasetId, String table) throws SQLException {
        addRecord(datasource, table, new String[] { null, datasetId + "", "0", "", "", "true" });
    }

}
