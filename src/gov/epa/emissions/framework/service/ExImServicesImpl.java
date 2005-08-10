/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: EMFDataService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.service;


import java.util.Date;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.ExImServices;
import gov.epa.emissions.framework.commons.EMFConstants;
import gov.epa.emissions.framework.commons.Status;
import gov.epa.emissions.framework.commons.StatusServices;

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
        
        /*
         * Since the ExImTask is creating the status messages in a seperate thread
         * Send in an instance of the statusService and a template status object for
         * the user that invoked this service
         */
        
        //FIXME:  Replace with factory method to get the status service
        StatusServices statusSvc = new StatusServicesImpl();
                
        ExImTask eximTask = new ExImTask(userName,fileName,fileType, statusSvc);
        eximTask.run();
        eximTask = null;
        System.out.println("In ExImServicesImpl:startImport");
    }

}
