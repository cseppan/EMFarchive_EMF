/*
 * Created on Aug 11, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.client.dummy;
 * File Name: EMFGenericClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.dummy;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ExImServicesTransport;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.client.transport.StatusServicesTransport;
import gov.epa.emissions.framework.client.transport.UserServicesTransport;
import gov.epa.emissions.framework.commons.EMFConstants;
import gov.epa.emissions.framework.commons.Status;
import gov.epa.emissions.framework.commons.User;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFGenericClient {

    ServiceLocator svcLoc;
    
    /**
     * @throws EmfException
     * 
     */
    public EMFGenericClient() throws EmfException {
        super();
        svcLoc = ServiceLocator.getInstance();
        doEmfSvcs();
    }

    /**
     * @throws EmfException
     * 
     */
    private void doEmfSvcs() throws EmfException {
      UserServicesTransport usersSvc = svcLoc.getUsersService(EMFConstants.HOST_NAME_BEN_ID);
      User user = usersSvc.getUser("cdcruz");
      System.out.println(user.getFullName());

      StatusServicesTransport statusSvc = svcLoc.getStatusService(EMFConstants.HOST_NAME_BEN_ID);
      Status[] allStats = statusSvc.getMessages("cdcruz");
      
      System.out.println("Number of messages: " + allStats.length);
      ExImServicesTransport eximSvc = svcLoc.getEximService(EMFConstants.HOST_NAME_BEN_ID);
      eximSvc.startImport("cdcruz","conrad.txt","TST");
      
    }//doEmfSvcs

    public static void main(String[] args) throws EmfException {
        new EMFGenericClient();
    }
}
