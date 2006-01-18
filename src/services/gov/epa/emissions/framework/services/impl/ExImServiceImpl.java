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
import gov.epa.emissions.framework.dao.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EMFConstants;
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

    private String baseImportFolder = null;

    private String baseExportFolder = null;

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

        // FIXME: Get base directory
        File mountPoint = new File(getValue(EMFConstants.EMF_DATA_ROOT_FOLDER));
        File importFolder = new File(mountPoint, getValue(EMFConstants.EMF_DATA_IMPORT_FOLDER));
        File exportFolder = new File(mountPoint, getValue(EMFConstants.EMF_DATA_EXPORT_FOLDER));

        baseImportFolder = importFolder.getAbsolutePath();
        baseExportFolder = exportFolder.getAbsolutePath();

        importerFactory = new VersionedImporterFactory(dbServer, dbServer.getSqlDataTypes());
        exporterFactory = new VersionedExporterFactory(dbServer, dbServer.getSqlDataTypes());

        // TODO: thread pooling policy
        threadPool = new PooledExecutor(new BoundedBuffer(10), 20);
        threadPool.setMinimumPoolSize(3);
    }

    private String getValue(String root) {
        Session session = sessionFactory.getSession();
        EmfPropertiesDAO dao = new EmfPropertiesDAO();
        String propvalue = dao.getProperty(root, session).getValue();
        session.close();

        return propvalue;
    }

    private File validateExportFile(File path, String fileName, boolean overwrite) throws EmfException {
        log.debug("check if file exists " + fileName);
        File file = new File(path, fileName);

        if (!overwrite) {
            if (file.exists() && file.isFile()) {
                log.error("File exists and cannot be overwritten");
                throw new EmfException("Cannot export to existing file.  Choose overwrite option");
            }
        }

        log.debug("check if file exists " + fileName);

        return file;
    }

    private File validatePath(String folderPath) throws EmfException {
        log.debug("check if folder exists " + folderPath);
        File file = new File(folderPath);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + folderPath + " does not exist");
            throw new EmfException("Folder does not exist: "+folderPath);
        }
        log.debug("check if folder exists " + folderPath);
        return file;
    }

    private void validateDatasetName(EmfDataset dataset) throws EmfException {
        log.debug("check if dataset name exists in table: " + dataset.getName());
        Session session = sessionFactory.getSession();
        boolean dsNameUsed = new DatasetDao().exists(dataset.getName(), session);
        session.flush();
        session.close();
        if (dsNameUsed) {
            log.error("Dataset name " + dataset.getName() + " is already used");
            throw new EmfException("Dataset name is already used");
        }
        log.debug("check if dataset name exists in table: " + dataset.getName());
    }

    public void startImport(User user, String folderPath, String fileName, EmfDataset dataset) throws EmfException {
        log.debug("In ExImServicesImpl:startImport START for: " + dataset.getDatasetid() + " " + dataset.getName());

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
            throw new EmfException("Failed to import file: " + e.getMessage());
        }

        log.debug("In ExImServicesImpl:startImport END");
    }

    public void startExportWithOverwrite(User user, EmfDataset[] datasets, String dirName, String purpose)
            throws EmfException {
        log.info("Start: exporting datasets" + user.getUsername());
        log.info("Total number of files to export: " + datasets.length);
        File path = validatePath(dirName);

        try {
            for (int i = 0; i < datasets.length; i++) {

                EmfDataset dataset = datasets[i];

                // FIXME: Default is overwrite
                File file = validateExportFile(path, getCleanDatasetName(dataset), true);
                Services svcHolder = new Services();
                svcHolder.setLogSvc(new LoggingServiceImpl(sessionFactory));
                svcHolder.setStatusSvc(new StatusServiceImpl(sessionFactory));
                svcHolder.setDataSvc(new DataServiceImpl(sessionFactory));
                Exporter exporter = exporterFactory.create(dataset, dataset.getDefaultVersion());
                AccessLog accesslog = new AccessLog(user.getUsername(), dataset.getDatasetid(), dataset
                        .getAccessedDateTime(), "Version " + dataset.getDefaultVersion(), purpose, dirName);
                ExportTask eximTask = new ExportTask(user, file, dataset, svcHolder, accesslog, exporter);
                threadPool.execute(eximTask);
            }
        } catch (Exception e) {
            log.error("Exception attempting to start export of file to folder: " + dirName, e);
            throw new EmfException(e.getMessage());
        }

        log.info("Start export for user: " + user.getUsername());
    }

    public void startExport(User user, EmfDataset[] datasets, String dirName, String purpose) throws EmfException {
        log.info("Start: exporting datasets" + user.getUsername());
        log.info("Total number of files to export: " + datasets.length);
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
                    AccessLog accesslog = new AccessLog(user.getUsername(), dataset.getDatasetid(), dataset
                            .getAccessedDateTime(), "Version " + dataset.getDefaultVersion(), purpose, dirName);
                    ExportTask eximTask = new ExportTask(user, file, dataset, svcHolder, accesslog, exporter);
                    threadPool.execute(eximTask);
                }

            }
        } catch (Exception e) {
            log.error("Exception attempting to start export of file to folder: " + dirName, e);
            throw new EmfException(e.getMessage());
        }

        log.info("Start export for user: " + user.getUsername());
    }

    private boolean isExportable(EmfDataset dataset) {
        boolean exportable = false;

        String datasetType = dataset.getDatasetType().getName();

        if (!((datasetType.equals("Shapefile")) || (datasetType.equals("External File"))
                || (datasetType.equals("Meteorology File")) || (datasetType.equals("Model Ready Emissions File")))) {

            exportable = true;
        }
        return exportable;
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

        return prefix + name + "_" + date + suffix;
    }

    public String getImportBaseFolder() {
        return baseImportFolder;
    }

    public String getExportBaseFolder() {
        return baseExportFolder;
    }

    public void startMultipleFileImport(User user, String folderPath, String[] fileNames, DatasetType datasetType) throws EmfException {
        log.debug("multiple datasets import: " + datasetType.getName());
        String[] fileNamesForImport=null;
        
        try {
            File folder = validatePath(folderPath);

            if (fileNames.length==1) {
                String fileName = fileNames[0];

                // The fileName is a regular expression that maps to a collection of files.
                FilePatternMatcher fpm = new FilePatternMatcher(fileName);
                String[] allFilesInFolder = folder.list();
                fileNamesForImport = fpm.matchingNames(allFilesInFolder);                
            }

            // Loop through the collection and start import for the file
            for (int i = 0; i < fileNamesForImport.length; i++) {
                String fileName = fileNamesForImport[i];
                log.debug("#### Filename: " + fileName);
                String importFileName = fileName + "_" + DATE_FORMATTER.format(new Date());
                log.debug("modified filename: " + importFileName);
                EmfDataset dataset = new EmfDataset();
                dataset.setName(importFileName);
                dataset.setCreator(user.getFullName());
                dataset.setDatasetType(datasetType);
                dataset.setCreatedDateTime(new Date());
                dataset.setModifiedDateTime(new Date());
                dataset.setAccessedDateTime(new Date());
                
                startImport(user, folderPath, fileName, dataset);            
            }
            
        } catch (ImporterException e) {
            log.error("Exception attempting to start export of file to folder: " + folderPath, e);
            throw new EmfException(e.getMessage());
       }
        
       
    }


}
