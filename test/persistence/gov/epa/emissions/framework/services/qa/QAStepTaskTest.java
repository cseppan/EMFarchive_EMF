package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.QAStepTask;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.File;
import java.util.List;

public class QAStepTaskTest extends ServiceTestCase {
    
    private UserDAO userDAO;
    
    private DbServer dbserver;
    
    private String tableName = "test" + Math.round(Math.random() * 1000) % 1000;

    protected void doSetUp() throws Exception {
        userDAO = new UserDAO();
        dbserver = dbServer();
    }
    
    protected void doTearDown() throws Exception {// no op
        //dropAll(Version.class);
        //dropAll(QAStep.class);
    }
    
    public void testShouldGetDefaultSummaryQANames() throws Exception {
        EmfDataset dataset = newDataset(tableName, "");
        User user = userDAO.get("emf", session);
        
        QAStepTask qaTask = new QAStepTask(dataset, 0, user, sessionFactory(), dbServer());
        String[] summaryQANames = qaTask.getDefaultSummaryQANames();
        
        try {
            assertEquals(3, summaryQANames.length);
            assertEquals("Summarize by Pollutant", summaryQANames[0]);
            assertEquals("Summarize by SCC and Pollutant", summaryQANames[1]);
            assertEquals("Summarize by County and Pollutant", summaryQANames[2]);
        } finally {
            remove(dataset);
        }
    }
    
    public void testShouldCheckAndRunSummaryQASteps() throws Exception {
        EmfDataset inputDataset = new EmfDataset();
        inputDataset.setName(tableName);
        inputDataset.setCreator(userDAO.get("emf", session).getUsername());
        inputDataset.setDatasetType(getDatasetType("ORL Nonpoint Inventory (ARINV)"));
        inputDataset = addORLNonpointDataset(inputDataset);
        
        addVersionZeroEntryToVersionsTable(inputDataset, dbserver.getEmissionsDatasource());
        
        try {
            QAStepTask qaTask = new QAStepTask(inputDataset, 0, userDAO.get("emf", session), sessionFactory(), dbserver);
            String[] summaryQANames = qaTask.getDefaultSummaryQANames();
            qaTask.runSummaryQASteps(summaryQANames);
        } finally {
            remove(inputDataset);
        } 
    }
    
    private void addVersionZeroEntryToVersionsTable(Dataset dataset, Datasource datasource) throws Exception {
        TableModifier modifier = new TableModifier(datasource, "versions");
        String[] data = { null, dataset.getId() + "", "0", "Initial Version", "", "true", null };
        modifier.insertOneRow(data);
    }
    
    private EmfDataset newDataset(String name, String type) {
        User owner = userDAO.get("emf", session);
        
        if (type.equals(""))
            type = "ORL Nonpoint Inventory (ARINV)";
        
        EmfDataset dataset = new EmfDataset();
        dataset.setName(name);
        dataset.setCreator(owner.getUsername());
        dataset.setDatasetType(getDatasetType(type));
        
        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

    private DatasetType getDatasetType(String type) {
        DataCommonsDAO dcDao = new DataCommonsDAO();
        List types = dcDao.getDatasetTypes(session);

        for (int i = 0; i < types.size(); i++)
            if (((DatasetType)types.get(i)).getName().equalsIgnoreCase(type))
                return (DatasetType)types.get(i);
            
        return null;
    }
    
    private EmfDataset addORLNonpointDataset(EmfDataset inputDataset) throws ImporterException {
        DbServer dbServer = dbServer();
        SqlDataTypes sqlDataTypes = dbServer.getSqlDataTypes();
        Version version = new Version();
        version.setName("Initial Version");
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/cost");
        String[] fileNames = { "orl-nonpoint-with-larger_values.txt" };
        ORLNonPointImporter importer = new ORLNonPointImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, tableName);
    }
}
