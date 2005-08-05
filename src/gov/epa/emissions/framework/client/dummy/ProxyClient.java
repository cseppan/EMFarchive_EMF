/*
 * Created on Jul 21, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.client.dummy;
 * File Name: ProxyClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.dummy;

import gov.epa.emissions.framework.client.transport.Proxy;
import gov.epa.emissions.framework.commons.UserServices;
import gov.epa.emissions.framework.commons.User;

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
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
    	try
    	{
    	    Class[] interfaces = new Class[] {UserServices.class};
    	    UserServices emfAdmin = (UserServices)(Proxy.newInstance("urn:EMFUserAdmin",interfaces));
    	    User user = emfAdmin.getUser("cdcruz");
    	    System.out.println(user.getFullName());

    	}
    	catch(Exception e)
    	{
    	    e.printStackTrace();
    	}       
    }
}
