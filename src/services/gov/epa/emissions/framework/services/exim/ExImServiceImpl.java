package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import javax.sql.DataSource;

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

    public void exportDatasets(User user, EmfDataset[] datasets, String dirName, String purpose) throws EmfException {
        exportService.export(user, datasets, dirName, purpose, false);
    }

    public void exportDatasetsWithOverwrite(User user, EmfDataset[] datasets, String dirName, String purpose)
            throws EmfException {
        exportService.export(user, datasets, dirName, purpose, true);
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

}
