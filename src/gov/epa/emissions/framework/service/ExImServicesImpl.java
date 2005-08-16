/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: EMFDataService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.service;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFConstants;
import gov.epa.emissions.framework.commons.ExImServices;
import gov.epa.emissions.framework.commons.StatusServices;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class ExImServicesImpl implements ExImServices{

    /**
     * 
     */
    public ExImServicesImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFData#startImport(java.lang.String, java.lang.String)
     */
    public void startImport(String userName, String fileName, String fileType) throws EmfException {

        System.out.println("In ExImServicesImpl:startImport");

        File file = null;
        
        try {
            file = checkFile(fileName);
            
            /*
             * Since the ExImTask is creating the status messages in a seperate thread
             * Send in an instance of the statusService and the username of
             * the user that invoked this service
             */
            
            //FIXME:  Replace with factory method to get the status service
            StatusServices statusSvc = new StatusServicesImpl();
            
            //FixMe: Create DbServer object and pass it into the ExImTask
            //DbServer dbServer
            ExImTask eximTask = new ExImTask(userName,file,fileType, statusSvc);
            eximTask.run();
            eximTask = null;
        } catch (EmfException e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
        System.out.println("In ExImServicesImpl:startImport");
    }//startImport

    /**
     * @param fileName
     * @throws EmfException
     */
    private File checkFile(String fileName) throws EmfException{
        String uriPathName="/usr/local/emf_data";
        File file = null;
        
        try {
            URI uri = new URI(EMFConstants.URI_FILENAME_PREFIX + uriPathName);
            File filePath = new File(uri);
            
            file = new File(filePath,fileName);

            if (!file.exists()) throw new Exception("foobar");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new EmfException("Pathname invalid: " + uriPathName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("File not found: " + uriPathName + "/" + fileName);
        }
        return file;

        
    }//checkFile

}//ExImServicesImpl
