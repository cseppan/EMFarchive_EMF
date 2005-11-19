package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.SimpleDataset;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.db.DbUpdate;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DbRecord;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.services.WebServicesIntegrationTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

public class DataEditorServiceTest extends WebServicesIntegrationTestCase {

    private DataEditorService services;
    protected DatabaseSetup dbSetup;

    protected File fieldDefsFile;

    protected File referenceFilesDir;

    private Datasource datasource;

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private String datasetName="test";
    
    protected void setUp() {
        try {
            databaseSetup();
            services = serviceLocator.getDataEditorService();
            DbServer dbServer = dbSetup.getDbServer();
            sqlDataTypes = dbServer.getDataType();
            datasource = dbServer.getEmissionsDatasource();

            dataset = new SimpleDataset();
            dataset.setName(datasetName);
            dataset.setDatasetid(new Random().nextLong());

            ORLNonPointImporter importer = new ORLNonPointImporter(dataset, datasource, sqlDataTypes);

            importer.preCondition(new File("test/data/orl/nc"), "small-nonpoint.txt");
            importer.run(dataset);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ImporterException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void databaseSetup() throws FileNotFoundException, IOException, SQLException {
        String folder = "test";
        File conf = new File(folder, "test.conf");

        if (!conf.exists() || !conf.isFile()) {
            String error = "File: " + conf + " does not exist. Please copy either of the two TEMPLATE files "
                    + "(from " + folder + "), name it test.conf, configure " + "it as needed, and rerun.";
            throw new RuntimeException(error);
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(conf));

        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");

        dbSetup = new DatabaseSetup(properties);
        fieldDefsFile = new File("config/field_defs.dat");
        referenceFilesDir = new File("config/refDbFiles");
        
    }

    public void testShouldReturnExactlyOnePage() throws EmfException {
         Page page = services.getPage(datasetName,1);
        assertTrue(page != null);
    }

    public void testShouldReturnAtleastOneRecord() throws EmfException {
        int numberOfRecords = services.getTotalRecords(datasetName);
       assertTrue(numberOfRecords >=1);
   }

    public void testShouldReturnAtLeastOnePage() throws EmfException{
          int numberOfPages=services.getPageCount(datasetName);
          assertTrue(numberOfPages >=1);
    }

    /**
     * This test gets a page using an integer record ID.  The resulting collection that is
     * acquired from the page should contain the record with record id that was supplied.
     * 
     * @throws EmfException
     */
    public void testShouldReturnOnlyOnePage() throws EmfException{
        int numberOfRecords = services.getTotalRecords(datasetName);
        
        Page page = services.getPageWithRecord(datasetName,numberOfRecords-1);
        DbRecord[] allRecs = page.getRecords();
        boolean found=false;
        
        for (int i=0; i<allRecs.length; i++){
            if (allRecs[i].getId()==numberOfRecords-1){
                found=true;
            }
        }
        assertTrue(found);
  }


    /**
     * This test tries gets a page using an integer record ID.  The page should be null
     * 
     * @throws EmfException
     */
    public void testShouldReturnNoPage() throws EmfException{
        int numberOfRecords = services.getTotalRecords(datasetName);
        
        Page page = services.getPageWithRecord(datasetName,numberOfRecords+1);
        DbRecord[] allRecs = page.getRecords();
        boolean found=false;
        
        for (int i=0; i<allRecs.length; i++){
            if (allRecs[i].getId()==numberOfRecords+1){
                found=true;
            }
        }
        assertTrue(!found);
  }
    
    
    /**
     * The tear down method will clean up the dataset and tables
     * 
     */
    protected void tearDown() throws Exception {
        DbUpdate dbUpdate = new DbUpdate(datasource.getConnection());
        dbUpdate.dropTable(datasource.getName(), dataset.getName());
        dbSetup.tearDown();

    }


}
