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
import gov.epa.emissions.framework.commons.EMFData;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFDataService implements EMFData{

    /**
     * 
     */
    public EMFDataService() {
        super();
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFData#startImport(java.lang.String, java.lang.String)
     */
    public void startImport(String fileName, String fileType) throws EmfException {
        System.out.println("In EMFDataService:startImport");
        EMFImporterService impSvc = new EMFImporterService(fileName,fileType);
        impSvc.start();
        
        System.out.println("In EMFDataService:startImport");
    }

}
