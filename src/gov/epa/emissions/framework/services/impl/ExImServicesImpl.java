/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: EMFDataService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetTypesDAO;
import gov.epa.emissions.framework.services.DatasetType;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class ExImServicesImpl implements ExImServices{

    private static Log log = LogFactory.getLog(ExImServicesImpl.class);

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
        log.debug("In ExImServicesImpl:startImport begin");
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
            log.error("EmfException: ", e);
            throw new EmfException(e.getMessage());
        }
        log.debug("In ExImServicesImpl:startImport END");
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
            log.error("EmfException: ", e);
            throw new EmfException("Pathname invalid: " + uriPathName);
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new EmfException("File not found: " + uriPathName + "/" + fileName);
        }
        return file;

        
    }//checkFile

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.ExImServices#startImport(gov.epa.emissions.framework.commons.User, java.lang.String, gov.epa.emissions.framework.commons.DatasetType)
     */
    public void startImport(User user, String fileName, DatasetType datasetType) throws EmfException {

        log.debug("In ExImServicesImpl:startImport START");

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
            ExImTask eximTask = new ExImTask(user,file,datasetType, statusSvc);
            eximTask.run();
            eximTask = null;
        } catch (EmfException e) {
            log.error("EMFException", e);
            throw new EmfException(e.getMessage());
        }
        log.debug("In ExImServicesImpl:startImport END");
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.ExImServices#getDatasetTypes()
     */
    public DatasetType[] getDatasetTypes() throws EmfException {
        log.debug("In ExImServicesImpl:getDatasetTypes START");

    	Session session = HibernateUtils.currentSession();
        List datasettypes = DatasetTypesDAO.getDatasetTypes(session);
        log.debug("In ExImServicesImpl:getDatasetTypes END");
        return (DatasetType[]) datasettypes.toArray(new DatasetType[datasettypes.size()]);    
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.services.ExImServices#insertDatasetType(gov.epa.emissions.framework.services.DatasetType)
     */
    public void insertDatasetType(DatasetType aDst) throws EmfException {
        log.debug("In ExImServicesImpl:insertDatasetType START");

    	Session session = HibernateUtils.currentSession();
        DatasetTypesDAO.insertDatasetType(aDst,session);
        log.debug("In ExImServicesImpl:insertDatasetType END");

    }

}//ExImServicesImpl
