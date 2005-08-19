/*
 * Created on Jul 18, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.client.dummy;
 * File Name: EMFWSDLClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Service;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFWSDLClient {
    private static String endpoint = 
        "http://ben.cep.unc.edu:8080/emf/services/EMFUserManagerService";

    /**
     * 
     */
    public EMFWSDLClient() {
        super();
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        Service  service = new Service();
        URL url;
        try {
            url = new URL(endpoint);
            EMFUserManagerServiceSoapBindingStub emfstub = new EMFUserManagerServiceSoapBindingStub(url,service);
            Object[] users = emfstub.getUsers();
            
            System.out.println(users.length);
            
            //User user = emfstub.getUser("ejones");
            //System.out.println(user.getFullName());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
