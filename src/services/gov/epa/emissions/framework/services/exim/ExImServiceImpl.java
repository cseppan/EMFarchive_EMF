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
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;

import javax.sql.DataSource;

import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExImServiceImpl extends EmfServiceImpl implements ExImService {
    private static int svcCount = 0;

    private String svcLabel = null;

    public String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private PooledExecutor threadPool;


// Comment out the next line when TaskManagedImportService is completed and tested
    private ImportService importService;
// This will be the task managed import service 
// Uncomment the next line when TaskManagedImportService is completed and tested    
//    private TaskManagedImportService importService;

    // private ExportService exportService;
    private ManagedExportService exportService;

    public ExImServiceImpl() throws Exception {
        super("ExIm Service");
        init(dbServer, HibernateSessionFactory.get());
        myTag();
        if (DebugLevels.DEBUG_4)
            System.out.println(">>>> " + myTag());
    }

    protected void finalize() throws Throwable {
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();

        svcCount--;
        if (DebugLevels.DEBUG_4)
            System.out.println(">>>> Destroying object: " + myTag());
        super.finalize();
    }

    public ExImServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory) {
        super(datasource, dbServer);
        init(dbServer, sessionFactory);
        myTag();
        if (DebugLevels.DEBUG_4)
            System.out.println(myTag());

    }

    private void init(DbServer dbServer, HibernateSessionFactory sessionFactory) {
        threadPool = createThreadPool();

        setProperties(sessionFactory);

        // Use the TaskManagedExport Service instead of the old ad-hoc thread pool ExportService
        // exportService = new ExportService(dbServer, threadPool, sessionFactory);
        exportService = new ManagedExportService(dbServer, sessionFactory);

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
        if (DebugLevels.DEBUG_4)
            System.out.println(">>## calling export datasets in eximSvcImp: " + myTag() + " for datasets: "
                    + datasets.toString());
        String submitterId = exportService.exportForClient(user, datasets, versions, dirName, purpose, false);
        if (DebugLevels.DEBUG_4)
            System.out.println("In ExImServiceImpl:exportDatasets() SUBMITTERID= " + submitterId);
    }

    public void exportDatasetsWithOverwrite(User user, EmfDataset[] datasets, Version[] versions, String dirName,
            String purpose) throws EmfException {
        if (DebugLevels.DEBUG_4)
            System.out.println(">>## calling export datasets with overwrite in eximSvcImp: " + myTag()
                    + " for datasets: " + datasets.toString());
        String submitterId = exportService.exportForClient(user, datasets, versions, dirName, purpose, true);
        if (DebugLevels.DEBUG_4)
            System.out.println("In ExImServiceImpl:exportDatasetsWithOverwrite() SUBMITTERID= " + submitterId);
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
        int numOfDS = datasetIds.length;
        EmfDataset[] datasets = new EmfDataset[numOfDS];
        DataServiceImpl ds = new DataServiceImpl();

        // Sift through and choose only those whose id matches
        // one of the list of dataset ids in the array.
        /** * this has caused the sequence of the retrieved datasets is different from the original one in datasetIds ** */

        // for (int i=0; i<rawDatasets.length;i++){
        // if (dsetIds.contains(new Integer (rawDatasets[i].getId()))){
        // rawDatasets[i].setAccessedDateTime(new Date());
        // ar.add(rawDatasets[i]);
        // }

        for (int i = 0; i < numOfDS; i++)
            datasets[i] = ds.getDataset(new Integer(datasetIds[i]));

        // if Vservion[] is not specified, get the default versions from datasets themselves
        if (versions == null) {
            Version[] defaultVersions = new Version[numOfDS];

            for (int j = 0; j < numOfDS; j++)
                defaultVersions[j] = getVersion(datasets[j], datasets[j].getDefaultVersion());

            exportDatasets(user, datasets, defaultVersions, folder, purpose);
            return;
        }

        // Invoke the local method that uses the datasets
        exportDatasets(user, datasets, versions, folder, purpose);
    }

    // FIXME: DELETE AFTER MERGED CHANGES HAVE BEEN TESTED AND VERIFIED
    // public void exportDatasetids(User user, Integer[] datasetIds, Version[] versions, String folder, String purpose)
    // throws EmfException {
    // EmfDataset[] datasets = null;
    //
    // // Using paramterized generic ArrayList
    // ArrayList<EmfDataset> ar = new ArrayList<EmfDataset>();
    // EmfDataset[] rawDatasets = null;
    // List<Integer> dsetIds = Arrays.asList(datasetIds);
    //
    // // get all the datasets from the dataservice.
    // DataServiceImpl ds = new DataServiceImpl();
    // rawDatasets = ds.getDatasets();
    //
    // // Sift through and choose only those whose id matches
    // // one of the list of dataset ids in the array.
    //
    // for (int i = 0; i < rawDatasets.length; i++) {
    // if (dsetIds.contains(new Integer(rawDatasets[i].getId()))) {
    // rawDatasets[i].setAccessedDateTime(new Date());
    // ar.add(rawDatasets[i]);
    // }
    // }
    //
    // datasets = ar.toArray(new EmfDataset[0]);
    // // Invoke the local method that uses the datasets
    // exportDatasets(user, datasets, versions, folder, purpose);
    //
    // }

    public void exportDatasetidsWithOverwrite(User user, Integer[] datasetIds, Version[] versions, String folder,
            String purpose) throws EmfException {

        int numOfDS = datasetIds.length;
        EmfDataset[] datasets = new EmfDataset[numOfDS];
        DataServiceImpl ds = new DataServiceImpl();

        // Sift through and choose only those whose id matches
        // one of the list of dataset ids in the array.
        /** * this has caused the sequence of the retrieved datasets is different from the original one in datasetIds ** */

        // for (int i=0; i<rawDatasets.length;i++){
        // if (dsetIds.contains(new Integer (rawDatasets[i].getId()))){
        // rawDatasets[i].setAccessedDateTime(new Date());
        // ar.add(rawDatasets[i]);
        // }

        for (int i = 0; i < numOfDS; i++)
            datasets[i] = ds.getDataset(new Integer(datasetIds[i]));

        // if Vservion[] is not specified, get the default versions from datasets themselves
        if (versions == null) {
            Version[] defaultVersions = new Version[numOfDS];

            for (int j = 0; j < numOfDS; j++)
                defaultVersions[j] = getVersion(datasets[j], datasets[j].getDefaultVersion());

            exportDatasetsWithOverwrite(user, datasets, defaultVersions, folder, purpose);
            return;
        }

        // Invoke the local method that uses the datasets
        exportDatasetsWithOverwrite(user, datasets, versions, folder, purpose);
    }

    // FIXME: DELETE AFTER MERGED CHANGES HAVE BEEN TESTED AND VERIFIED
    // public void exportDatasetidsWithOverwrite(User user, Integer[] datasetIds, Version[] versions, String folder,
    // String purpose) throws EmfException {
    // EmfDataset[] datasets = null;
    //
    // // Using paramterized generic ArrayList
    // ArrayList<EmfDataset> ar = new ArrayList<EmfDataset>();
    // EmfDataset[] rawDatasets = null;
    // List<Integer> dsetIds = Arrays.asList(datasetIds);
    //
    // // get all the datasets from the dataservice.
    // DataServiceImpl ds = new DataServiceImpl();
    // rawDatasets = ds.getDatasets();
    //
    // // Sift through and choose only those whose id matches
    // // one of the list of dataset ids in the array.
    //
    // for (int i = 0; i < rawDatasets.length; i++) {
    // if (dsetIds.contains(new Integer(rawDatasets[i].getId()))) {
    // rawDatasets[i].setAccessedDateTime(new Date());
    // ar.add(rawDatasets[i]);
    // }
    // }
    // datasets = ar.toArray(new EmfDataset[0]);
    //
    // // Invoke the local method that uses the datasets
    // exportDatasetsWithOverwrite(user, datasets, versions, folder, purpose);
    // }

    // Export NO overwrite using default dataset version
    public void exportDatasetids(User user, Integer[] datasetIds, String folder, String purpose) throws EmfException {
        // if Vservion[] is not specified, get the default versions from datasets themselves
        exportDatasetids(user, datasetIds, null, folder, purpose);
    }

    // Export with overwrite using default dataset version

    public void exportDatasetidsWithOverwrite(User user, Integer[] datasetIds, String folder, String purpose)
            throws EmfException {
        exportDatasetidsWithOverwrite(user, datasetIds, null, folder, purpose);
    }


}
