package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.PerformanceMetrics;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExImServiceImpl extends EmfServiceImpl implements ExImService {

    private static Log LOG = LogFactory.getLog(ExImServiceImpl.class);

    private VersionedExporterFactory exporterFactory;

    private PooledExecutor threadPool;

    private ImportService importService;

    private ExportService exportService;

    public ExImServiceImpl() throws Exception {
        init(dbServer, HibernateSessionFactory.get());
        LOG.debug("creating ExImService - " + this.hashCode());
    }

    protected void finalize() throws Throwable {
        LOG.debug("closing ExImService - " + this.hashCode());
        new PerformanceMetrics().gc();
        super.finalize();
    }

    public ExImServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory) {
        super(datasource, dbServer);
        init(dbServer, sessionFactory);
    }

    private void init(DbServer dbServer, HibernateSessionFactory sessionFactory) {
        threadPool = createThreadPool();

        exporterFactory = new VersionedExporterFactory(dbServer, dbServer.getSqlDataTypes());
        exportService = new ExportService(exporterFactory, threadPool, sessionFactory);

        ImporterFactory importerFactory = new ImporterFactory(dbServer, dbServer.getSqlDataTypes());
        importService = new ImportService(importerFactory, sessionFactory, threadPool);
    }

    private PooledExecutor createThreadPool() {
        // TODO: thread pooling policy
        PooledExecutor threadPool = new PooledExecutor(new BoundedBuffer(10), 20);
        threadPool.setMinimumPoolSize(3);
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
