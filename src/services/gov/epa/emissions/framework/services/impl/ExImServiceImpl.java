package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

import javax.sql.DataSource;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExImServiceImpl extends EmfServiceImpl implements ExImService {

    private VersionedExporterFactory exporterFactory;

    private PooledExecutor threadPool;

    private ImportService importService;

    private ExportService exportService;

    public ExImServiceImpl() throws Exception {
        init(dbServer, HibernateSessionFactory.get());
    }

    public ExImServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory) {
        super(datasource, dbServer);
        init(dbServer, sessionFactory);
    }

    private void init(DbServer dbServer, HibernateSessionFactory sessionFactory) {
        // TODO: thread pooling policy
        threadPool = new PooledExecutor(new BoundedBuffer(10), 20);
        threadPool.setMinimumPoolSize(3);

        exporterFactory = new VersionedExporterFactory(dbServer, dbServer.getSqlDataTypes());
        exportService = new ExportService(exporterFactory, threadPool, sessionFactory);

        ImporterFactory importerFactory = new ImporterFactory(dbServer, dbServer.getSqlDataTypes());
        importService = new ImportService(importerFactory, sessionFactory, threadPool);
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
