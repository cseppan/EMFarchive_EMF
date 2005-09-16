/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: EMFDataService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.exporter.Exporter;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDAO;
import gov.epa.emissions.framework.dao.DatasetTypesDAO;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class ExImServicesImpl implements ExImServices {

    private static Log log = LogFactory.getLog(ExImServicesImpl.class);

    private ImporterFactory importerFactory;

    private ExporterFactory exporterFactory;
    
    private String baseImportFolder=null;
    private String baseExportFolder=null;

    public ExImServicesImpl() throws NamingException, SQLException {
        // TODO: should we move this into an abstract super class ?
        Context ctx = new InitialContext();
        DataSource datasource = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");

        //FIXME: Get base directory
        File baseFile=new File(System.getProperty("user.dir"),"emf");
        File baseImportFile=new File(baseFile,"import");
        File baseExportFile=new File(baseFile,"export");
        baseImportFolder=baseImportFile.getAbsolutePath();
        baseExportFolder=baseExportFile.getAbsolutePath();
        
        // FIXME: we should not hard-code the db server. Also, read the
        // datasource names from properties
        DbServer dbServer = new PostgresDbServer(datasource.getConnection(), "reference", "emissions");

        importerFactory = new ImporterFactory(dbServer);
        exporterFactory = new ExporterFactory(dbServer);
    }

    private File validateExportFile(String fileName, boolean overwrite) throws EmfException {
        log.debug("check if file exists " + fileName);
        File file = new File(fileName);

        if (!overwrite){
            if (file.exists() && file.isFile()) {
                log.error("File exists and cannot be overwritten");
                throw new EmfException("Cannot export to existing file.  Choose overwrite option");
            }        	
        }
        
        log.debug("check if file exists " + fileName);

        return file;
    }

    private File validateFile(File folder, String fileName) throws EmfException {
        log.debug("check if file exists " + fileName);
        File file = new File(folder, fileName);

        if (!file.exists() || !file.isFile()) {
            log.error("File " + file.getAbsolutePath() + " not found");
            throw new EmfException("File not found");
        }
        log.debug("check if file exists " + fileName);

        return file;
    }

    private File validatePath(String folderPath) throws EmfException {
        log.debug("check if folder exists " + folderPath);
        File file = new File(folderPath);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + folderPath + " does not exist");
            throw new EmfException("Folder does not exist");
        }
        log.debug("check if folder exists " + folderPath);
        return file;
    }

    private void validateDatasetName(EmfDataset dataset) throws EmfException {
        log.debug("check if dataset name exists in table: " + dataset.getName());
        Session session = EMFHibernateUtil.getSession();
        boolean dsNameUsed = DatasetDAO.isDatasetNameUsed(dataset.getName(), session);
        session.flush();
        session.close();
        if (dsNameUsed) {
            log.error("Dataset name " + dataset.getName() + " is already used");
            throw new EmfException("Dataset name is already used");
        }
        log.debug("check if dataset name exists in table: " + dataset.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.services.ExImServices#startImport(gov.epa.emissions.framework.services.User,
     *      java.lang.String, gov.epa.emissions.commons.io.EmfDataset,
     *      gov.epa.emissions.commons.io.DatasetType)
     */
    public void startImport(User user, String folderPath, String fileName, EmfDataset dataset, DatasetType datasetType)
            throws EmfException {
        log.debug("In ExImServicesImpl:startImport START");

        try {
            File path = validatePath(folderPath);
            File file = validateFile(path, fileName);
            validateDatasetName(dataset);
            StatusServices statusSvc = new StatusServicesImpl();
            DataServices dataSvc = new DataServicesImpl();

            Importer importer = importerFactory.create(datasetType);
            ImportTask eximTask = new ImportTask(user, file, dataset, datasetType, dataSvc, statusSvc, importer);

            // FIXME: use a thread pool
            new Thread(eximTask).start();
        } catch (Exception e) {
            log.error("Exception attempting to start import of file: " + fileName, e);
            throw new EmfException(e.getMessage());
        }

        log.debug("In ExImServicesImpl:startImport END");
    }

    public void startExport(User user, EmfDataset[] datasets, String dirName, boolean overwrite) throws EmfException {
        log.info("Start export for user: " + user.getUsername());
        int count = datasets.length;
        log.info("Total number of files to export: " + count);
        StatusServices statusSvc = new StatusServicesImpl();
        File path = validatePath(dirName);

        try {
            for (int i = 0; i < count; i++) {

                EmfDataset aDataset = datasets[i];

                // FIXME: Default is overwrite
                //File file = new File(path, getCleanDatasetName(aDataset.getName()));
                File file = validateExportFile(getCleanDatasetName(aDataset.getName()),overwrite);
                Exporter exporter = exporterFactory.create(aDataset.getDatasetType());
                ExportTask eximTask = new ExportTask(user, file, aDataset, statusSvc, exporter);
                new Thread(eximTask).start();
            }
        } catch (Exception e) {
            log.error("Exception attempting to start export of file to folder: " + dirName, e);
            throw new EmfException(e.getMessage());
        }

        log.info("Start export for user: " + user.getUsername());
    }

    private String getCleanDatasetName(String name) {
        String cleanName = null;
        StringBuffer sbuf = new StringBuffer();

        StringTokenizer stok = new StringTokenizer(name, " ", true);

        while (stok.hasMoreTokens()) {
            String tok = stok.nextToken();
            if (tok.equals(" ")) {
                tok = "_";
            }

            sbuf.append(tok);

        }
        cleanName = sbuf.toString() + ".txt";

        // FIXME: Is Error checking for empty name needed?

        return cleanName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.services.ExImServices#startExport(gov.epa.emissions.framework.services.User,
     *      gov.epa.emissions.commons.io.Dataset, java.lang.String)
     */
    public void startExport(User user, EmfDataset dataset, String fileName) throws EmfException {
        log.debug("In ExImServicesImpl:startExport START");

        try {
            File file = new File(fileName);
            StatusServices statusSvc = new StatusServicesImpl();
            Exporter exporter = exporterFactory.create(dataset.getDatasetType());
            ExportTask eximTask = new ExportTask(user, file, dataset, statusSvc, exporter);

            // FIXME: use a thread pool
            new Thread(eximTask).start();
        } catch (Exception e) {
            log.error("Exception attempting to start export of file: " + fileName, e);
            throw new EmfException(e.getMessage());
        }

        log.debug("In ExImServicesImpl:startExport END");

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.services.ExImServices#getDatasetTypes()
     */
    public DatasetType[] getDatasetTypes() {
        log.debug("In ExImServicesImpl:getDatasetTypes START");

        // Session session = HibernateUtils.currentSession();
        Session session = EMFHibernateUtil.getSession();
        List datasettypes = DatasetTypesDAO.getDatasetTypes(session);
        log.debug("In ExImServicesImpl:getDatasetTypes END");
        session.flush();
        session.close();
        return (DatasetType[]) datasettypes.toArray(new DatasetType[datasettypes.size()]);
    }

    public void insertDatasetType(DatasetType aDst) {
        log.debug("In ExImServicesImpl:insertDatasetType START");

        // Session session = HibernateUtils.currentSession();
        Session session = EMFHibernateUtil.getSession();
        DatasetTypesDAO.insertDatasetType(aDst, session);
        session.flush();
        session.close();
        log.debug("In ExImServicesImpl:insertDatasetType END");

    }

	public String getImportBaseFolder() throws EmfException {
		
		//FIXME:
		boolean broke=false;
		if (broke) throw new EmfException("TEMP");
		
		
		
		return baseImportFolder;
	}

	public String getExportBaseFolder() throws EmfException {

		//FIXME:
		boolean broke=false;
		if (broke) throw new EmfException("TEMP");
		
		
		return baseExportFolder;
	}

}
