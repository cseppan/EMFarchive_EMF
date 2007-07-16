package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.EmfServiceImpl;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExImServiceImpl extends EmfServiceImpl implements ExImService {

    private PooledExecutor threadPool;

    private ImportService importService;

    private ExportService exportService;

    public ExImServiceImpl() throws Exception {
        super("ExIm Service");
        init(dbServer, HibernateSessionFactory.get());
    }

    protected void finalize() throws Throwable {
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();
        super.finalize();
    }

    public ExImServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory) {
        super(datasource, dbServer);
        init(dbServer, sessionFactory);
    }

    private void init(DbServer dbServer, HibernateSessionFactory sessionFactory) {
        threadPool = createThreadPool();

        setProperties(sessionFactory);

        exportService = new ExportService(dbServer, threadPool, sessionFactory);

        ImporterFactory importerFactory = new ImporterFactory(dbServer, dbServer.getSqlDataTypes());
        importService = new ImportService(importerFactory, sessionFactory, threadPool);
    }

    private PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    private void setProperties(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty batchSize = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            EmfProperty eximTempDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", session);

            if (eximTempDir != null)
                System.setProperty("IMPORT_EXPORT_TEMP_DIR", eximTempDir.getValue());

            if (batchSize != null)
                System.setProperty("EXPORT_BATCH_SIZE", batchSize.getValue());
        } finally {
            session.close();
        }
    }

    public void exportDatasets(User user, EmfDataset[] datasets, Version[] versions, String dirName, String purpose)
            throws EmfException {
        exportService.export(user, datasets, versions, dirName, purpose, false);
    }

    public void exportDatasetsWithOverwrite(User user, EmfDataset[] datasets, Version[] versions, String dirName,
            String purpose) throws EmfException {
        exportService.export(user, datasets, versions, dirName, purpose, true);
    }

    public void importDatasets(User user, String folderPath, String[] filenames, DatasetType datasetType) {
        importService.importDatasets(user, folderPath, filenames, datasetType);
    }

    public void importDataset(User user, String folderPath, String[] filenames, DatasetType datasetType,
            String datasetName) throws EmfException {
        importService.importDataset(user, folderPath, filenames, datasetType, datasetName);
    }

    public String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException {
        return importService.getFilenamesFromPattern(folder, pattern);
    }

    public Version getVersion(Dataset dataset, int version) throws EmfException {
        return exportService.getVersion(dataset, version);
    }

    public void exportDatasetids(User user, Integer[] datasetIds, Version[] versions, String folder, String purpose)
            throws EmfException {
        EmfDataset[] datasets=null;
        
        //Using paramterized generic ArrayList
        ArrayList<EmfDataset> ar = new ArrayList<EmfDataset>();
        EmfDataset[] rawDatasets=null;
        List<Integer> dsetIds = Arrays.asList(datasetIds);
        
        // get all the datasets from the dataservice.  
        DataServiceImpl ds = new DataServiceImpl();
        rawDatasets=ds.getDatasets();
        
        // Sift through and choose only those whose id matches
        // one of the list of dataset ids in the array.
        
        for (int i=0; i<rawDatasets.length;i++){
            if (dsetIds.contains(new Integer (rawDatasets[i].getId()))){
                rawDatasets[i].setAccessedDateTime(new Date());
                ar.add(rawDatasets[i]);
            }
        }
        
        datasets = ar.toArray(new EmfDataset[0]);
        //Invoke the local method that uses the datasets
        exportDatasets(user, datasets, versions, folder, purpose);
        
    }

    public void exportDatasetidsWithOverwrite(User user, Integer[] datasetIds, Version[] versions, String folder,
            String purpose) throws EmfException {
        EmfDataset[] datasets=null;
        
        //Using paramterized generic ArrayList
        ArrayList<EmfDataset> ar = new ArrayList<EmfDataset>();
        EmfDataset[] rawDatasets=null;
        List<Integer> dsetIds = Arrays.asList(datasetIds);
        
        // get all the datasets from the dataservice.  
        DataServiceImpl ds = new DataServiceImpl();
        rawDatasets=ds.getDatasets();
        
        // Sift through and choose only those whose id matches
        // one of the list of dataset ids in the array.
        
        for (int i=0; i<rawDatasets.length;i++){
            if (dsetIds.contains(new Integer (rawDatasets[i].getId()))){
                rawDatasets[i].setAccessedDateTime(new Date());
                ar.add(rawDatasets[i]);
            }
        }
        datasets = ar.toArray(new EmfDataset[0]);
       
        //Invoke the local method that uses the datasets
        exportDatasetsWithOverwrite(user, datasets, versions, folder, purpose);
    }

}
