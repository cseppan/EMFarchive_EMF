/*
 * Created on Aug 11, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.client.dummy;
 * File Name: EMFGenericClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFGenericClient {

    RemoteServiceLocator svcLoc;
    
    /**
     * @throws EmfException
     * 
     */
    public EMFGenericClient() throws EmfException {
        super();
        svcLoc = new RemoteServiceLocator("http://ben.cep.unc.edu:8080/emf/services");
        doEmfSvcs();
    }

    /**
     * @throws EmfException
     * 
     */
    private void doEmfSvcs() throws EmfException {
      UserServices usersSvc = svcLoc.getUserServices();
      User user = usersSvc.getUser("cdcruz");
      System.out.println(user.getFullName());

      StatusServices statusSvc = svcLoc.getStatusServices();
      Status[] allStats = statusSvc.getMessages("cdcruz");
      
      System.out.println("Number of messages: " + allStats.length);
//      ExImServices eximSvc = svcLoc.getEximServices();
//      DatasetType[] datasetTypes = eximSvc.getDatasetTypes();
//      //TODO: lookup the 'TST' dataset type 
//      eximSvc.startImport(user,"conrad.txt", datasetTypes[0]/*"TST"*/);
//      
    }//doEmfSvcs

    public static void main(String[] args) throws EmfException {
        new EMFGenericClient();
    }
}
