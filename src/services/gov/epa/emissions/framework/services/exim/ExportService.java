package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExportService {
    private static Log log = LogFactory.getLog(ExportService.class);

    private VersionedExporterFactory exporterFactory;

    private HibernateSessionFactory sessionFactory;

    private PooledExecutor threadPool;

    private static String timeformat = "ddMMMyyyy";
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat(timeformat);

    public ExportService(DbServer dbServer, PooledExecutor threadPool,
            HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.exporterFactory = new VersionedExporterFactory(dbServer, dbServer.getSqlDataTypes(),batchSize());
        this.threadPool = threadPool;
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

    private Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusDAO(sessionFactory));
        services.setDataService(new DataServiceImpl(sessionFactory));

        return services;
    }

    private Status status(User user, String message) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("Export");
        status.setMessage(message);
        status.setTimestamp(new Date());
        return status;
    }

    private File validatePath(String folderPath) throws EmfException {
        File file = new File(folderPath);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + folderPath + " does not exist");
            throw new EmfException("Folder does not exist: " + folderPath);
        }
        return file;
    }

    private boolean isExportable(EmfDataset dataset, Services services, User user) {
        DatasetType datasetType = dataset.getDatasetType();

        if ((datasetType.getExporterClassName().equals("")) || (datasetType.getExporterClassName() == null)) {
            String message = "The exporter for dataset type '" + datasetType + " is not supported";
            Status status = status(user, message);
            services.getStatus().add(status);
            return false;
        }
        return true;
    }

    String getCleanDatasetName(EmfDataset dataset) {
        String name = dataset.getName();
        String prefix = "", suffix = "";
        KeyVal[] keyvals = dataset.getKeyVals();
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

    void export(User user, EmfDataset[] datasets, Version[] versions, String dirName, String purpose, boolean overwrite)
            throws EmfException {
        File path = validatePath(dirName);
        
        if(datasets.length != versions.length) {
            log.error("Export failed: version numbers doesn't match specified datasets.");
            throw new EmfException("Export failed: version numbers doesn't match " +
                    "specified datasets.");
        }

        try {
            for (int i = 0; i < datasets.length; i++) {
                Services services = services();
                EmfDataset dataset = datasets[i];
                Version version = versions[i];
                if (isExportable(dataset, services, user))
                    doExport(user, dirName, purpose, overwrite, path, dataset, version);
            }
        } catch (Exception e) {
            log.error("Export error- start exporting a file to folder: " + dirName, e);
            throw new EmfException("Export failed: " + e.getMessage());
        }
    }

    private void doExport(User user, String dirName, String purpose, boolean overwrite, File path, EmfDataset dataset, Version version)
            throws Exception {
        Services services = services();
        File file = validateExportFile(path, getCleanDatasetName(dataset), overwrite);
        Exporter exporter = exporterFactory.create(dataset, version);
        AccessLog accesslog = new AccessLog(user.getUsername(), dataset.getId(), dataset.getAccessedDateTime(),
                "Version " + dataset.getDefaultVersion(), purpose, file.getAbsolutePath());

        ExportTask eximTask = new ExportTask(user, file, dataset, services, accesslog, exporter, sessionFactory);
        threadPool.execute(new GCEnforcerTask("Export of Dataset: " + dataset.getName(), eximTask));
    }

    public Version getVersion(Dataset dataset, int version) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(dataset.getId(), version, session);
        } catch (Exception e) {
            log.error("Retrieve version error - can't retrieve Version object for dataset: " + dataset.getName(), e);
            throw new EmfException("Retrieve version error - can't retrieve Version object for dataset: " + dataset.getName() + " " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private int batchSize() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            return Integer.parseInt(property.getValue());
        } finally {
            session.close();
        }
    }
}
