package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;
import gov.epa.emissions.framework.services.exim.ExImServiceImpl;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExImServiceImplTestCase extends ExImServiceTestCase {

    private DbServer dbServer;

    protected void doSetUp() throws Exception {
        dbServer = dbServerFactory.getDbServer();
        ExImService eximService = new ExImServiceImpl(dbServerFactory, emf(), dbServer, sessionFactory);
        UserService userService = new UserServiceImpl(sessionFactory);
        DataCommonsServiceImpl commonsService = new DataCommonsServiceImpl(sessionFactory);

        super.setUpService(eximService, userService, commonsService);
    }

    protected void doTearDown() throws Exception {
        dropAll(InternalSource.class);
        dropAll(Version.class);
        dropAll(EmfDataset.class);
    }

    private void dropTables(InternalSource[] sources) throws Exception, SQLException {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);

        for (InternalSource source : sources) {
            dbUpdate.dropTable(datasource.getName(), source.getTable());
            System.out.println("Table : " + source.getTable() + " dropped.");
        }
    }

    public void testImportOrlNonPoint() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";

        eximService.importDatasets(user, repository.getAbsolutePath(), new String[] { filename }, dataset
                .getDatasetType());
        Thread.sleep(2000); // so that import thread has enough time to run

        EmfDataset[] imported = dataService.getDatasets();
        EmfDataset localdataset = imported[0];
        assertEquals(filename, imported[0].getName());
        assertEquals(1, imported.length);

        dropTables(localdataset.getInternalSources());
    }

    public void testImportSingleOrlNonPoint() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";

        eximService.importDataset(user, repository.getAbsolutePath(), new String[] { filename }, dataset
                .getDatasetType(), "small-nonpoint1.txt");
        Thread.sleep(2000); // so that import thread has enough time to run

        EmfDataset[] imported = dataService.getDatasets();
        EmfDataset localdataset = imported[0];
        assertEquals(filename, imported[0].getName());
        assertEquals(1, imported.length);

        dropTables(localdataset.getInternalSources());
    }

    //NOTE: Worked on Linux platform but hung on Widows platform for the next two cases.
    
    public void testImportMultipleOrlNonPoint() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename1 = "small-nonpoint1.txt";
        String filename2 = "small-nonpoint2.txt";
        String filename3 = "small-nonpoint3.txt";
        String filename4 = "small-nonpoint4.txt";

        String[] files = new String[] { filename1, filename2, filename3, filename4 };
        EmfDataset[] imported = null;

        try {
            eximService.importDatasets(user, repository.getAbsolutePath(), files, dataset.getDatasetType());
            Thread.sleep(240000); // so that import thread has enough time to run

            imported = dataService.getDatasets();
            List<String> importedNames = new ArrayList<String>();

            for (int i = 0; i < imported.length; i++)
                importedNames.add(imported[i].getName());

            assertTrue(importedNames.contains(filename1));
            assertTrue(importedNames.contains(filename2));
            assertTrue(importedNames.contains(filename3));
            assertTrue(importedNames.contains(filename4));
            assertEquals(4, imported.length);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            InternalSource[] sources = new InternalSource[imported.length];

            for (int i = 0; i < sources.length; i++) {
                sources[i] = imported[i].getInternalSources()[0];
            }

            dropTables(sources);
        }
    }

    public void testImportMultipleLineBasedDatasets() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");
        
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename1 = "small-nonpoint1.txt";
        String filename2 = "small-nonpoint2.txt";
        String filename3 = "small-nonpoint3.txt";
        String filename4 = "small-nonpoint4.txt";
        
        String[] files = new String[] { filename1, filename2, filename3, filename4 };
        EmfDataset[] imported = null;
        
        try {
            eximService.importDatasets(user, repository.getAbsolutePath(), files, getDatasetType("Text file (Line-based)"));
            Thread.sleep(240000); // so that import thread has enough time to run
            
            imported = dataService.getDatasets();
            List<String> importedNames = new ArrayList<String>();

            for (int i = 0; i < imported.length; i++)
                importedNames.add(imported[i].getName());

            assertTrue(importedNames.contains(filename1));
            assertTrue(importedNames.contains(filename2));
            assertTrue(importedNames.contains(filename3));
            assertTrue(importedNames.contains(filename4));
            assertEquals(4, imported.length);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            InternalSource[] sources = new InternalSource[imported.length];

            for (int i = 0; i < sources.length; i++) {
                sources[i] = imported[i].getInternalSources()[0];
            }

            dropTables(sources);
        }
    }

    public void testExportWithOverwrite() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        dataset.setDescription("description");
        dataset.setStatus("imported");

        // import
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";
        eximService.importDataset(user, repository.getAbsolutePath(), new String[] { filename }, dataset
                .getDatasetType(), filename);

        Thread.sleep(2000); // so that import thread has enough time to run

        EmfDataset[] imported = dataService.getDatasets();
        assertEquals(filename, imported[0].getName());

        Version version = new Version();
        version.setDatasetId(imported[0].getId());

        // export
        File outputFile = new File(System.getProperty("java.io.tmpdir"));
        outputFile.deleteOnExit();
        if (!outputFile.exists())
            outputFile.mkdir();

        eximService.exportDatasetsWithOverwrite(user, new EmfDataset[] { imported[0] }, new Version[] { version },
                outputFile.getAbsolutePath(), "Exporting NonPoint file");

        Thread.sleep(2000);// wait, until the export is complete

        dropTables(imported[0].getInternalSources());
    }

    public void testExport() throws Exception {
        DataService dataService = new DataServiceImpl(dbServerFactory, sessionFactory);
        User user = userService.getUser("emf");

        dataset.setDescription("description");
        dataset.setStatus("imported");

        // import
        File repository = new File(System.getProperty("user.dir"), "test/data/orl/nc/");
        String filename = "small-nonpoint1.txt";
        eximService.importDatasets(user, repository.getAbsolutePath(), new String[] { filename }, dataset
                .getDatasetType());

        Thread.sleep(2000); // so that import thread has enough time to run

        EmfDataset[] imported = dataService.getDatasets();
        assertEquals(filename, imported[0].getName());

        Version version = new Version();
        version.setDatasetId(imported[0].getId());

        // export
        File outputFile = new File(System.getProperty("java.io.tmpdir"));
        outputFile.deleteOnExit();
        if (!outputFile.exists())
            outputFile.mkdir();

        eximService.exportDatasets(user, new EmfDataset[] { imported[0] }, new Version[] { version }, outputFile
                .getAbsolutePath(), "Exporting NonPoint file");

        // FIXME: verify the exported file exists
        Thread.sleep(2000);// wait, until the export is complete

        dropTables(imported[0].getInternalSources());
    }

}
