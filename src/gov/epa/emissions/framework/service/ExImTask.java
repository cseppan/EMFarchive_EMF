/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: ImporterService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.service;

import java.util.Date;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFConstants;
import gov.epa.emissions.framework.commons.Status;
import gov.epa.emissions.framework.commons.StatusServices;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class ExImTask implements Runnable {

    private String userName;
    private String fileName;
    private String fileType;
    private StatusServices statusSvc = null;

    /**
     * 
     */
    public ExImTask(String fileName, String fileType) {
        super();
        this.fileName = fileName;
        this.fileType = fileType;
    }

    /**
     * @param fileName
     * @param fileType
     * @param statusSvc
     * @param status
     */
    public ExImTask(String userName, String fileName, String fileType, StatusServices statusSvc) {
       super();
       this.fileName = fileName;
       this.fileType=fileType;
       this.statusSvc=statusSvc;
       this.userName = userName;

    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      System.out.println("In NEW THREAD start import: " + fileName + " " + fileType);
      Status startStatus = new Status();
      startStatus.setUserName(this.userName);
      startStatus.setMsgType(EMFConstants.IMPORT_MESSAGE_TYPE);
      startStatus.setMessage(EMFConstants.START_IMPORT_MESSAGE_Prefix + fileType + ":" + fileName);
      startStatus.setTimestamp(new Date());
      try {
        statusSvc.setStatus(startStatus);
        
        System.out.println("In NEW THREAD start import: AFTER SETTING START IMPORT MESSAGE");
          
          //Dummy wait
          //Replace with instance of actual importer
//          try {
//            for (int i=0; i<100; i++){
//                  Thread.sleep(10);
//              }
//           } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//           }

       Status endStatus = new Status();
       endStatus.setUserName(this.userName);
       endStatus.setMsgType(EMFConstants.IMPORT_MESSAGE_TYPE);
       endStatus.setMessage(EMFConstants.END_IMPORT_MESSAGE_Prefix + fileType + ":" + fileName);
       endStatus.setTimestamp(new Date());

       statusSvc.setStatus(endStatus);
    } catch (EmfException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    
      System.out.println("In NEW THREAD end import: " + fileName + " " + fileType);
    }

}
