/*
 * Created on Aug 11, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.client.dummy;
 * File Name: EMFGenericClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

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
        svcLoc = new RemoteServiceLocator("http://localhost:8080/emf/services");
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
      ExImServices eximSvc = svcLoc.getEximServices();
      eximSvc.startImport("cdcruz","conrad.txt","TST");
      
    }//doEmfSvcs

    public static void main(String[] args) throws EmfException {
        new EMFGenericClient();
    }
}
