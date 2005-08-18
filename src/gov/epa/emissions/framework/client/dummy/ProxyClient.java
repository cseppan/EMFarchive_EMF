/*
 * Created on Jul 21, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.client.dummy;
 * File Name: ProxyClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.dummy;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.Proxy;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class ProxyClient {

    /**
     * 
     */
    public ProxyClient() {
        super();
    }

    public static void main(String[] args) {
    	    try {
                Class[] interfaces = new Class[] {UserServices.class};
                UserServices emfAdmin = (UserServices)(Proxy.newInstance("urn:gov.epa.emf.services.UserServices",interfaces));
                User user = emfAdmin.getUser("cdcruz");
                System.out.println(user.getFullName());
            } catch (EmfException e) {
                e.printStackTrace();
            } catch(Exception e){
    	    e.printStackTrace();
    	}    
   
    }
}
