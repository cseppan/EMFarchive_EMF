package gov.epa.emissions.framework.client.dummy;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.EMFDataTransport;
import gov.epa.emissions.framework.client.transport.UserServicesTransport;
import gov.epa.emissions.framework.commons.User;

/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: Hib
 * Package: 
 * File Name: ImporterClient.java
 * Author: Conrad F. D'Cruz
 */

/**
 * @author Conrad F. D'Cruz
 *
 */
public class UsersClient {
   
     private static String endpoint1 = "http://ben.cep.unc.edu:8080/emf/services/gov.epa.emf.UserServices";

    /**
     * @throws EmfException
     * 
     */
    public UsersClient() throws EmfException {
        super();
        UserServicesTransport userSvcs = new UserServicesTransport(endpoint1);
        User aUser = userSvcs.getUser("cdcruz");
        System.out.println(aUser.getFullName());
        User[] allusers = userSvcs.getUsers();
        for (int i=0; i<allusers.length;i++){
            System.out.println(allusers[i].getFullName());
        }
    }

    public static void main(String[] args) throws EmfException {
        new UsersClient();
    }
}
