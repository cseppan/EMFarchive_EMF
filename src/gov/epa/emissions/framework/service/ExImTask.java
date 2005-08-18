/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: ImporterService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.service;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.TableTypes;
import gov.epa.emissions.commons.io.importer.orl.ORLImporter;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DataSourceFactory;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class ExImTask implements Runnable {

    private String userName;
    private File file;
    private String fileType;
    private StatusServices statusSvc = null;

    /**
     * 
     */
    public ExImTask(File file, String fileType) {
        super();
        this.file = file;
        this.fileType = fileType;
    }

    /**
     * @param fileName
     * @param fileType
     * @param statusSvc
     * @param status
     */
    public ExImTask(String userName, File file, String fileType, StatusServices statusSvc) {
       super();
       this.file = file;
       this.fileType=fileType;
       this.statusSvc=statusSvc;
       this.userName = userName;

    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      System.out.println("In NEW THREAD start import: " + file.getName() + " " + fileType);
      Status startStatus = new Status();
      startStatus.setUserName(this.userName);
      startStatus.setMsgType(EMFConstants.IMPORT_MESSAGE_TYPE);
      startStatus.setMessage(EMFConstants.START_IMPORT_MESSAGE_Prefix + fileType + ":" + file.getName());
      startStatus.setTimestamp(new Date());
      try {
        statusSvc.setStatus(startStatus);
        
        System.out.println("In NEW THREAD start import: AFTER SETTING START IMPORT MESSAGE");
          
        // Call the importer
//		DummyImporter imptr = new DummyImporter(DataSourceFactory.getDataSource(),file);
//		imptr.run();

        // Create an instance of the EmfDataset
        Dataset dataset = new EmfDataset();
        String datasetType = DatasetTypes.ORL_AREA_NONPOINT_TOXICS;
        String tableType = TableTypes.ORL_AREA_NONPOINT_TOXICS;
        String filename = file.getName();
        String table = filename.substring(0, filename.length() - 4).replace('.', '_');
        
        dataset.setDatasetType(datasetType);
        dataset.addDataTable(tableType, table);
        String summaryTableType = DatasetTypes.getSummaryTableType(datasetType);
        dataset.addDataTable(summaryTableType, table + "_summary");

        // Get an instance of a DbServer
        DbServer dbServer = new PostgresDbServer(DataSourceFactory.getDataSource().getConnection(), "reference", "emissions");
        
        // Get the specific type of importer for the filetype requested by user for import
        ORLImporter importer = new ORLImporter(dbServer, false, true);
        
        // Invoke the importer for the specific file
        importer.run(new File[]{file},dataset, true);
        
		Status endStatus = new Status();
		endStatus.setUserName(this.userName);
		endStatus.setMsgType(EMFConstants.IMPORT_MESSAGE_TYPE);
		endStatus.setMessage(EMFConstants.END_IMPORT_MESSAGE_Prefix + fileType + ":" + file.getName());
		endStatus.setTimestamp(new Date());
		
		statusSvc.setStatus(endStatus);
    } catch (EmfException e) {
        e.printStackTrace();
    } catch (SQLException e) {
        // TODO Auto-generated catch block
        //EMF TODO: We need to get an EMF Exception thrown from below
        e.printStackTrace();
    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    
      System.out.println("In NEW THREAD end import: " + file.getName() + " " + fileType);
    }

}
