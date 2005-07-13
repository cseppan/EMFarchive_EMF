/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.service.axis;
  
import gov.epa.emissions.framework.client.transport.EMFUserAdmin;
import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFClient {

    /**
     * 
     */
    public EMFClient() {
        super();
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        String statuscode = null;
        
        EMFUserAdmin emfUserAdmin = new EMFUserAdminTransport();
        System.out.println("IN EMFCLIENT main");
        
        String uname = "ejones";
        String pwd = "erin123";
        
        statuscode = emfUserAdmin.authenticate(uname, pwd, true);        
        System.out.println(uname + " login status is: " + statuscode);
        statuscode = emfUserAdmin.authenticate(uname, pwd, false);        
        System.out.println(uname + " login status is: " + statuscode);

    }
}
