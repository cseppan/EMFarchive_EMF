/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: EMFUserAdminTransport.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.commons.EMFUser;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

/**
 * @author Conrad F. D'Cruz
 *
 *	This class implements the methods specified in the UserAdmin interface
 *
 */
public class EMFUserAdminTransport implements EMFUserAdmin {

    private static String endpoint = 
        "http://ben.cep.unc.edu:8080/emf/services/EMFUserManagerService";

    /**
     * 
     */
    public EMFUserAdminTransport() {
        super();
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#isNewUser()
     */
    public boolean isNewUser() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#validate()
     */
    public boolean validate() {
        // TODO Auto-generated method stub
        return false;
    }
    
    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#authenticate(java.lang.String, java.lang.String, boolean)
     */
    public String authenticate(String userName, String pwd,
            boolean wantAdminStatus) {

        String statusCode = null;
        
        Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            call.setOperationName(new QName("http://soapinterop.org/", "authenticate") );
            QName qname = new QName("urn:EMFUserManager","ns1:User");
            Class cls = gov.epa.emissions.framework.commons.User.class;
            call.registerTypeMapping(cls,qname,BeanSerializerFactory.class, BeanDeserializerFactory.class);

            call.addParameter("uname",
                               org.apache.axis.Constants.XSD_STRING,
                               javax.xml.rpc.ParameterMode.IN);

            call.addParameter("pwd",
                    org.apache.axis.Constants.XSD_STRING,
                    javax.xml.rpc.ParameterMode.IN);

            call.addParameter("wantAdmin",
                    org.apache.axis.Constants.XSD_BOOLEAN,
                    javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);

            statusCode = (String) call.invoke( new Object[] {userName, pwd, new Boolean(wantAdminStatus)} );

        } catch (ServiceException e) {
            System.out.println("Error invoking the service");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Error in format of URL string");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Error communicating with WS end point");
            e.printStackTrace();
        }

        return statusCode;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#resetPassword()
     */
    public boolean resetPassword() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#getUser(java.lang.String)
     */
    public User getUser(String userName) {
        User user = null;
        
        Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            call.setOperationName(new QName("http://soapinterop.org/", "getUser") );
            QName qname = new QName("urn:EMFUserManager","ns1:User");
            Class cls = gov.epa.emissions.framework.commons.User.class;
            call.registerTypeMapping(cls,qname,BeanSerializerFactory.class, BeanDeserializerFactory.class);

            call.addParameter("uname",
                               org.apache.axis.Constants.XSD_STRING,
                               javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(qname);
            user = (User) call.invoke( new Object[] {userName} );

        } catch (ServiceException e) {
            System.out.println("Error invoking the service");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Error in format of URL string");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Error communicating with WS end point");
            e.printStackTrace();
        }

        return user;

    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#createUser(gov.epa.emissions.framework.commons.User)
     */
    public String createUser(User newUser) {
        String statuscode = "null";
        Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            call.setOperationName(new QName("http://soapinterop.org/", "getUser") );
            QName qname = new QName("urn:EMFUserManager","ns1:User");
            Class cls = gov.epa.emissions.framework.commons.User.class;
            call.registerTypeMapping(cls,qname,BeanSerializerFactory.class, BeanDeserializerFactory.class);
            call.addParameter(qname,
                               org.apache.axis.Constants.XSD_ANY,
                               javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
            statuscode = (String) call.invoke( new Object[] {newUser} );

        } catch (ServiceException e) {
            System.out.println("Error invoking the service");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Error in format of URL string");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Error communicating with WS end point");
            e.printStackTrace();
        }

        return statuscode;

    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#updateUser(gov.epa.emissions.framework.commons.User)
     */
    public String updateUser(User newUser) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#deleteUser(java.lang.String)
     */
    public String deleteUser(String userName) {
        String statuscode = null;
               
        Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            call.setOperationName(new QName("http://soapinterop.org/", "deleteUser") );
            call.addParameter("uname",
                               org.apache.axis.Constants.XSD_STRING,
                               javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
            statuscode = (String) call.invoke( new Object[] {userName} );

        } catch (ServiceException e) {
            System.out.println("Error invoking the service");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Error in format of URL string");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Error communicating with WS end point");
            e.printStackTrace();
        }

        return statuscode;

    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#getUsers()
     */
    public User[] getUsers() {


            String statusCode = null;
            User[] users = null;;
            
            Service  service = new Service();
            Call     call;
            
            try {
                call = (Call) service.createCall();
                call.setTargetEndpointAddress( new java.net.URL(endpoint) );
                call.setOperationName(new QName("http://soapinterop.org/", "getUsers") );
                QName qname1 = new QName("urn:EMFUserManager","ns1:User");
                QName qname2 = new QName("urn:EMFUserManager","ns1:Users");
                //QName qname3 = new QName("urn:EMFUserManager","ArrayOf_xsd_anyType");
                
                Class cls1 = gov.epa.emissions.framework.commons.User.class;
                //Class cls2 = java.util.ArrayList.class;
                Class cls2 = gov.epa.emissions.framework.commons.User[].class;
                call.registerTypeMapping(cls1,qname1,BeanSerializerFactory.class, BeanDeserializerFactory.class);
                call.registerTypeMapping(cls2,qname2,ArraySerializerFactory.class, ArrayDeserializerFactory.class);
                call.setReturnType(qname2);
                
                Object obj = call.invoke( new Object[] {} );
                                
                users = (User[])obj;
                
            } catch (ServiceException e) {
                System.out.println("Error invoking the service");
                e.printStackTrace();
            } catch (MalformedURLException e) {
                System.out.println("Error in format of URL string");
                e.printStackTrace();
            } catch (RemoteException e) {
                System.out.println("Error communicating with WS end point");
                e.printStackTrace();
            }

            return users;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#updateUsers(java.util.Collection)
     */
    public String updateUsers(List users) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFUserAdmin#getEmfUsers()
     */
    public EMFUser[] getEmfUsers() {
        String statusCode = null;
        EMFUser[] users = null;;
        
        Service  service = new Service();
        Call     call;
        
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            call.setOperationName(new QName("http://soapinterop.org/", "getEmfUsers") );
            QName qname1 = new QName("urn:EMFUserManager","ns1:EMFUser");
            //QName qname3 = new QName("urn:EMFUserManager","ArrayOf_xsd_anyType");
            QName qname2 = new QName("urn:EMFUserManager","ns1:EMFUsers");
            
            Class cls1 = gov.epa.emissions.framework.commons.EMFUser.class;
            Class cls2 = gov.epa.emissions.framework.commons.EMFUser[].class;
            call.registerTypeMapping(cls1,qname1,BeanSerializerFactory.class, BeanDeserializerFactory.class);
            call.registerTypeMapping(cls2,qname2,ArraySerializerFactory.class, ArrayDeserializerFactory.class);
            
            call.setReturnType(qname2);
            Object obj = call.invoke( new Object[] {} );
            users = (EMFUser[])obj;
            
        } catch (ServiceException e) {
            System.out.println("Error invoking the service");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Error in format of URL string");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Error communicating with WS end point");
            e.printStackTrace();
        }

        return users;
    }


}
