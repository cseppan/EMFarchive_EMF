package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDao;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

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

    private VersionedImporterFactory importerFactory;

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

        importerFactory = new VersionedImporterFactory(dbServer, dbServer.getSqlDataTypes());
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
        boolean dsNameUsed = new DatasetDao().nameUsed(dataset.getName(), EmfDataset.class,session);
        session.flush();
        session.close();
        if (dsNameUsed) {
            log.error("Dataset name " + dataset.getName() + " is already used");
            throw new EmfException("Dataset name is already used");
        }
    }

    public void startImport(User user, String folderPath, String fileName, EmfDataset dataset) throws EmfException {
        try {
            File path = validatePath(folderPath);

            validateDatasetName(dataset);
            Services svcHolder = new Services();
            svcHolder.setDataSvc(new DataServiceImpl(sessionFactory));
            svcHolder.setStatusSvc(new StatusServiceImpl(sessionFactory));

            Importer importer = importerFactory.create(dataset, path, fileName);
            ImportTask eximTask = new ImportTask(user, fileName, dataset, svcHolder, importer);

            threadPool.execute(eximTask);
        } catch (Exception e) {
            log.error("Exception attempting to start import of file: " + fileName, e);
            throw new EmfException("Import of file failed: " + e.getMessage());
        }
    }

    public void startExportWithOverwrite(User user, EmfDataset[] datasets, String dirName, String purpose)
            throws EmfException {
        File path = validatePath(dirName);

        try {
            for (int i = 0; i < datasets.length; i++) {

                EmfDataset dataset = datasets[i];

                // if dataset is not exportable throw exception
                if (isExportable(dataset)) {
                    // FIXME: Default is overwrite
                    File file = validateExportFile(path, getCleanDatasetName(dataset), true);
                    Services svcHolder = new Services();
                    svcHolder.setLogSvc(new LoggingServiceImpl(sessionFactory));
                    svcHolder.setStatusSvc(new StatusServiceImpl(sessionFactory));
                    svcHolder.setDataSvc(new DataServiceImpl(sessionFactory));
                    Exporter exporter = exporterFactory.create(dataset, dataset.getDefaultVersion());
                    AccessLog accesslog = new AccessLog(user.getUsername(), dataset.getId(), dataset
                            .getAccessedDateTime(), "Version " + dataset.getDefaultVersion(), purpose, dirName);
                    ExportTask eximTask = new ExportTask(user, file, dataset, svcHolder, accesslog, exporter);
                    threadPool.execute(eximTask);
                }
            }
        } catch (Exception e) {
            log.error("Exception attempting to start export of file to folder: " + dirName, e);
            throw new EmfException("Exception attempting to start export of file to folder");
        }
    }

    public void startExport(User user, EmfDataset[] datasets, String dirName, String purpose) throws EmfException {
        File path = validatePath(dirName);

        try {
            for (int i = 0; i < datasets.length; i++) {

                EmfDataset dataset = datasets[i];

                // if dataset is not exportable throw exception
                if (isExportable(dataset)) {
                    // FIXME: Default is overwrite
                    File file = validateExportFile(path, getCleanDatasetName(dataset), false);
                    Services svcHolder = new Services();
                    svcHolder.setLogSvc(new LoggingServiceImpl(sessionFactory));
                    svcHolder.setStatusSvc(new StatusServiceImpl(sessionFactory));
                    svcHolder.setDataSvc(new DataServiceImpl(sessionFactory));
                    Exporter exporter = exporterFactory.create(dataset, dataset.getDefaultVersion());
                    AccessLog accesslog = new AccessLog(user.getUsername(), dataset.getId(), dataset
                            .getAccessedDateTime(), "Version " + dataset.getDefaultVersion(), purpose, dirName);
                    ExportTask eximTask = new ExportTask(user, file, dataset, svcHolder, accesslog, exporter);
                    threadPool.execute(eximTask);
                }

            }
        } catch (Exception e) {
            log.error("Exception attempting to start export of file to folder: " + dirName, e);
            throw new EmfException("Exception attempting to start export of file to folder");
        }
    }

    private boolean isExportable(EmfDataset dataset) {
        boolean exportable = true;
        
        DatasetType dst = dataset.getDatasetType();
        
        
        if ((dst.getExporterClassName().equals("")) || (dst.getExporterClassName()==null) ) exportable=false;
        
        return (exportable);
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

    public void startMultipleFileImport(User user, String folderPath, String[] fileNames, DatasetType datasetType)
            throws EmfException {
        String[] fileNamesForImport = null;
        
        try {
            File folder = validatePath(folderPath);

            if (fileNames.length == 1) {
                String fileName = fileNames[0];

                // The fileName is a regular expression that maps to a collection of files.
                FilePatternMatcher fpm = new FilePatternMatcher(fileName);
                String[] allFilesInFolder = folder.list();
                fileNamesForImport = fpm.matchingNames(allFilesInFolder);
            }

            // Loop through the collection and start import for the file
            for (int i = 0; i < fileNamesForImport.length; i++) {
                String fileName = fileNamesForImport[i];
                String importFileName = fileName + "_" + DATE_FORMATTER.format(new Date());
                EmfDataset dataset = new EmfDataset();
                dataset.setName(importFileName);
                dataset.setCreator(user.getUsername());
                dataset.setDatasetType(datasetType);
                dataset.setCreatedDateTime(new Date());
                dataset.setModifiedDateTime(new Date());
                dataset.setAccessedDateTime(new Date());

                startImport(user, folderPath, fileName, dataset);
            }

        } catch (ImporterException e) {
            log.error("Exception attempting to start export of file to folder: " + folderPath, e);
            throw new EmfException("Exception attempting to start export of file to folder");
        }

    }

}
