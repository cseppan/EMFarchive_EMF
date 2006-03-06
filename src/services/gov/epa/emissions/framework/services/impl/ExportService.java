package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Status;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExportService {
    private static Log log = LogFactory.getLog(ExportService.class);

    private VersionedExporterFactory exporterFactory;

    private HibernateSessionFactory sessionFactory;

    private PooledExecutor threadPool;

    public ExportService(VersionedExporterFactory exporterFactory, PooledExecutor threadPool,
            HibernateSessionFactory sessionFactory) {
        this.exporterFactory = exporterFactory;
        this.sessionFactory = sessionFactory;
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
        services.setLogSvc(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusServiceImpl(sessionFactory));
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
            services.getStatus().create(status);
            return false;
        }
        return true;
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

    void export(User user, EmfDataset[] datasets, String dirName, String purpose, boolean overwrite)
            throws EmfException {
        File path = validatePath(dirName);

        try {
            for (int i = 0; i < datasets.length; i++) {
                EmfDataset dataset = datasets[i];
                Services services = services();
                if (isExportable(dataset, services, user))
                    doExport(user, dirName, purpose, overwrite, path, dataset);
            }
        } catch (Exception e) {
            log.error("Export error- start exporting a file to folder: " + dirName, e);
            throw new EmfException("Export failed: " + e.getMessage());
        }
    }

    private void doExport(User user, String dirName, String purpose, boolean overwrite, File path, EmfDataset dataset)
            throws Exception {
        Services services = services();
        File file = validateExportFile(path, getCleanDatasetName(dataset), overwrite);
        Exporter exporter = exporterFactory.create(dataset, dataset.getDefaultVersion());
        AccessLog accesslog = new AccessLog(user.getUsername(), dataset.getId(), dataset.getAccessedDateTime(),
                "Version " + dataset.getDefaultVersion(), purpose, dirName);

        ExportTask eximTask = new ExportTask(user, file, dataset, services, accesslog, exporter);
        threadPool.execute(eximTask);
    }
}
