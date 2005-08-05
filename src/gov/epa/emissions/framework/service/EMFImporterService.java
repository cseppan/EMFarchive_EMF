/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: ImporterService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.service;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFImporterService extends Thread {

    private String fileName;
    private String fileType;

    /**
     * 
     */
    public EMFImporterService(String fileName, String fileType) {
        super();
        this.fileName = fileName;
        this.fileType = fileType;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      System.out.println("In NEW THREAD start import: " + fileName + " " + fileType);

      //Dummy wait
      //Replace with instance of actual importer
      try {
        for (int i=0; i<100; i++){
              Thread.sleep(10);
          }
    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
      System.out.println("In NEW THREAD end import: " + fileName + " " + fileType);
    }

}
