package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDao;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.Status;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExImServiceImpl extends EmfServiceImpl implements ExImService {

    private static Log log = LogFactory.getLog(ExImServiceImpl.class);

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMddyy_HHmm");

    private ImporterFactory importerFactory;

    private VersionedExporterFactory exporterFactory;

    private PooledExecutor threadPool;

    private HibernateSessionFactory sessionFactory;

    public ExImServiceImpl() throws Exception {
        init(dbServer, HibernateSessionFactory.get());
    }

    public ExImServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory) {
        super(datasource, dbServer);
        init(dbServer, sessionFactory);
    }

    private void init(DbServer dbServer, HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;

        importerFactory = new ImporterFactory(dbServer, dbServer.getSqlDataTypes());
        exporterFactory = new VersionedExporterFactory(dbServer, dbServer.getSqlDataTypes());

        // TODO: thread pooling policy
        threadPool = new PooledExecutor(new BoundedBuffer(10), 20);
        threadPool.setMinimumPoolSize(3);
    }

    private File validateExportFile(File path, String fileName, boolean overwrite) throws EmfException {
        File file = new File(path, fileName);

        if (!overwrite) {
            if (file.exists() && file.isFile()) {
                log.error("File exists and cannot be overwritten");
                throw new EmfException("Cannot export to existing file.  Choose overwrite option");
            }
        }
        return file;
    }

    private File validatePath(String folderPath) throws EmfException {
        File file = new File(folderPath);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + folderPath + " does not exist");
            throw new EmfException("Folder does not exist: " + folderPath);
        }
        return file;
    }

    private void validateDatasetName(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        boolean nameUsed = new DatasetDao().nameUsed(dataset.getName(), EmfDataset.class, session);
        session.flush();
        session.close();
        if (nameUsed) {
            log.error("Dataset name " + dataset.getName() + " is already used");
            throw new EmfException("Dataset name is already used");
        }
    }

    public void importSingleDataset(User user, String folderPath, String[] fileNames, EmfDataset dataset)
            throws EmfException {
        try {
            File path = validatePath(folderPath);

            validateDatasetName(dataset);
            Importer importer = importerFactory.createVersioned(dataset, path, fileNames);
            ImportTask eximTask = new ImportTask(user, fileNames, dataset, services(), importer);

            threadPool.execute(eximTask);
        } catch (Exception e) {
            log.error("Exception attempting to start import of file: " + fileNames[0], e);
            throw new EmfException("Import failed: " + e.getMessage());
        }
    }

    public void startExport(User user, EmfDataset[] datasets, String dirName, String purpose) throws EmfException {
        export(user, datasets, dirName, purpose, false);
    }

    public void startExportWithOverwrite(User user, EmfDataset[] datasets, String dirName, String purpose)
            throws EmfException {
        export(user, datasets, dirName, purpose, true);
    }

    private void export(User user, EmfDataset[] datasets, String dirName, String purpose, boolean overwrite)
            throws EmfException {
        File path = validatePath(dirName);

        try {
            for (int i = 0; i < datasets.length; i++) {

                EmfDataset dataset = datasets[i];
                Services services = services();
                if (isExportable(dataset, services, user)) {
                    File file = validateExportFile(path, getCleanDatasetName(dataset), overwrite);
                    Exporter exporter = exporterFactory.create(dataset, dataset.getDefaultVersion());
                    AccessLog accesslog = new AccessLog(user.getUsername(), dataset.getId(), dataset
                            .getAccessedDateTime(), "Version " + dataset.getDefaultVersion(), purpose, dirName);
                    ExportTask eximTask = new ExportTask(user, file, dataset, services, accesslog, exporter);
                    threadPool.execute(eximTask);
                }
            }
        } catch (Exception e) {
            log.error("Export error- start exporting a file to folder: " + dirName, e);
            Throwable cause = e.getCause();
            String message = (cause == null) ? e.getMessage() : cause.getMessage();
            throw new EmfException("Export Error: " + message);
        }
    }

    private Services services() {
        Services services = new Services();
        services.setLogSvc(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusServiceImpl(sessionFactory));
        services.setDataService(new DataServiceImpl(sessionFactory));

        return services;
    }

    private boolean isExportable(EmfDataset dataset, Services services, User user) {
        DatasetType datasetType = dataset.getDatasetType();

        if ((datasetType.getExporterClassName().equals("")) || (datasetType.getExporterClassName() == null)) {
            String message = "The exporter for dataset type '" + datasetType + " is not supported";
            Status status = status(user, message);
            services.getStatus().create(status);
            return false;
        }
        return true;
    }

    private Status status(User user, String message) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("Export");
        status.setMessage(message);
        status.setTimestamp(new Date());
        return status;
    }

    String getCleanDatasetName(EmfDataset dataset) {
        String name = dataset.getName();
        String prefix = "", suffix = "";
        KeyVal[] keyvals = dataset.getKeyVals();
        String timeformat = "ddMMMyyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(timeformat);
        String date = sdf.format(new Date());

        for (int i = 0; i < keyvals.length; i++) {
            prefix = keyvals[i].getKeyword().getName().equalsIgnoreCase("EXPORT_PREFIX") ? keyvals[i].getValue() : "";
            if (!prefix.equals(""))
                break;
        }

        for (int i = 0; i < keyvals.length; i++) {
            suffix = keyvals[i].getKeyword().getName().equalsIgnoreCase("EXPORT_SUFFIX") ? keyvals[i].getValue() : "";
            if (!suffix.equals(""))
                break;
        }

        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i))) {
                name = name.replace(name.charAt(i), '_');
            }
        }

        return prefix + name + "_" + date.toLowerCase() + suffix;
    }

    public void importDatasets(User user, String folderPath, String[] filenames, DatasetType datasetType) throws EmfException {
        for (int i = 0; i < filenames.length; i++) {
            String datasetName = filenames[i] + "_" + DATE_FORMATTER.format(new Date());
            EmfDataset dataset = new EmfDataset();
            dataset.setName(datasetName);
            dataset.setCreator(user.getUsername());
            dataset.setDatasetType(datasetType);
            dataset.setCreatedDateTime(new Date());
            dataset.setModifiedDateTime(new Date());
            dataset.setAccessedDateTime(new Date());

            importSingleDataset(user, folderPath, new String[]{filenames[i]}, dataset);
        }
    }
    
    public void importDataset(User user, String folderPath, String[] filenames, DatasetType datasetType, String datasetName) throws EmfException {
            EmfDataset dataset = new EmfDataset();
            dataset.setName(datasetName);
            dataset.setCreator(user.getUsername());
            dataset.setDatasetType(datasetType);
            dataset.setCreatedDateTime(new Date());
            dataset.setModifiedDateTime(new Date());
            dataset.setAccessedDateTime(new Date());

            importSingleDataset(user, folderPath, filenames, dataset);
    }

}
