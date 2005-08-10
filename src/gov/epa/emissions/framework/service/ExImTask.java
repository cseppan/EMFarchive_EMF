/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: ImporterService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.service;

import java.io.File;
import java.util.Date;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.DummyImporter;
import gov.epa.emissions.framework.commons.EMFConstants;
import gov.epa.emissions.framework.commons.Status;
import gov.epa.emissions.framework.commons.StatusServices;

import gov.epa.emissions.framework.dao.DataSourceFactory;;

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
          
        //Call the importer
		DummyImporter imptr = new DummyImporter(DataSourceFactory.getDataSource(),file);
		imptr.run();
        
		Status endStatus = new Status();
		endStatus.setUserName(this.userName);
		endStatus.setMsgType(EMFConstants.IMPORT_MESSAGE_TYPE);
		endStatus.setMessage(EMFConstants.END_IMPORT_MESSAGE_Prefix + fileType + ":" + file.getName());
		endStatus.setTimestamp(new Date());
		
		statusSvc.setStatus(endStatus);
    } catch (EmfException e) {
        e.printStackTrace();
    }
    
      System.out.println("In NEW THREAD end import: " + file.getName() + " " + fileType);
    }

}
