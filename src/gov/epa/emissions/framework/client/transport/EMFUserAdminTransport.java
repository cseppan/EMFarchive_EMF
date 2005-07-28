/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: EMFUserAdminTransport.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.AuthenticationException;
import gov.epa.emissions.framework.CommunicationFailureException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

/**
 * @author Conrad F. D'Cruz
 *
 *	This class implements the methods specified in the UserAdmin interface
 *
 */
public class EMFUserAdminTransport implements EMFUserAdmin {

    private static String endpoint = "";

    /**
     * 
     */
    public EMFUserAdminTransport() {
        super();
    }

    public EMFUserAdminTransport(String endpt) {
        super();
        endpoint=endpt;
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
    
    /**
     * 
     * This utility method extracts the significat message from the Axis Fault
     * @param faultReason
     * @return
     */
    private String extractMessage(String faultReason) {
        String message = faultReason.substring(faultReason.indexOf("Exception: ") + 11);
        if (message.equals("Connection refused: connect")){
            message = "Cannot communicate with EMF Server";
        }
        
        return message;
    }
   
    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#authenticate(java.lang.String, java.lang.String, boolean)
     */
    public void authenticate(String userName, String pwd,
            boolean wantAdminStatus) throws EmfException {
        
        Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setMaintainSession(true);
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

            call.invoke( new Object[] {userName, pwd, new Boolean(wantAdminStatus)} );

        } catch (ServiceException e) {
//            e.printStackTrace();
            throw new CommunicationFailureException("Error invoking the service");
        } catch (MalformedURLException e) {
//            e.printStackTrace();
            throw new CommunicationFailureException("Error in format of URL string");
        } catch(AxisFault fault) {
            throw new AuthenticationException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
//            e.printStackTrace();
            throw new CommunicationFailureException("Error communicating with WS end point");
        }
        
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
    public User getUser(String userName) throws CommunicationFailureException {
        User user = null;
        
        Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            
            QName qname1 = new QName("urn:EMFUserManager","ns1:User");
            QName qname2 = new QName("urn:WsAdminService", "getUser");
            call.setOperationName(qname2);
            Class cls = gov.epa.emissions.framework.commons.User.class;
	        call.registerTypeMapping(User.class, qname1,
	    			  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls, qname1),        
	    			  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls, qname1));        			  

            call.addParameter("uname",
                               org.apache.axis.Constants.XSD_STRING,
                               javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(qname1);
            user = (User) call.invoke( new Object[] {userName} );

        } catch (ServiceException e) {
            e.printStackTrace();
            throw new CommunicationFailureException("Error invoking the service");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new CommunicationFailureException("Error in format of URL string");
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new CommunicationFailureException("Error communicating with WS end point");
        }
        return user;

    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#createUser(gov.epa.emissions.framework.commons.User)
     */
    public void createUser(User newUser) throws CommunicationFailureException, AuthenticationException {
        String statuscode = "success";
        Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            call.setOperationName(new QName("urn:EMFUserManagerService", "createUser") );
            QName qname = new QName("urn:EMFUserManagerService","ns1:User");
            Class cls = gov.epa.emissions.framework.commons.User.class;
            call.registerTypeMapping(cls, qname,
					   new org.apache.axis.encoding.ser.BeanSerializerFactory(cls, qname),        
					   new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls, qname)); 

            call.addParameter( "newuser", qname, ParameterMode.IN );

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
            statuscode = (String) call.invoke( new Object[] {newUser} );

        } catch (ServiceException e) {
            throw new CommunicationFailureException("Error invoking the service");
        } catch (MalformedURLException e) {
            throw new CommunicationFailureException("Error in format of URL string");
        } catch(AxisFault fault) {
            throw new AuthenticationException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            throw new CommunicationFailureException("Error communicating with WS end point");
        }

        

    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#updateUser(gov.epa.emissions.framework.commons.User)
     */
    public void updateUser(User newUser) {
        String statuscode = "success";
        Service  service = new Service();
        Call     call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            call.setOperationName(new QName("urn:EMFUserManagerService", "updateUser") );
            QName qname = new QName("urn:EMFUserManagerService","ns1:User");
            Class cls = gov.epa.emissions.framework.commons.User.class;
            call.registerTypeMapping(cls, qname,
					   new org.apache.axis.encoding.ser.BeanSerializerFactory(cls, qname),        
					   new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls, qname)); 

            call.addParameter( "updateuser", qname, ParameterMode.IN );

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
            statuscode = (String) call.invoke( new Object[] {newUser} );

        } catch (ServiceException e) {
            System.out.println("Error invoking the service");
            e.printStackTrace();
            statuscode="failed";
        } catch (MalformedURLException e) {
            System.out.println("Error in format of URL string");
            e.printStackTrace();
            statuscode="failed";
        } catch (RemoteException e) {
            System.out.println("Error communicating with WS end point");
            e.printStackTrace();
            statuscode="failed";
        } 
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#deleteUser(java.lang.String)
     */
    public void deleteUser(String userName) {
        String statuscode = "success";
               
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
            statuscode="failed";
        } catch (MalformedURLException e) {
            System.out.println("Error in format of URL string");
            e.printStackTrace();
            statuscode="failed";
        } catch (RemoteException e) {
            System.out.println("Error communicating with WS end point");
            e.printStackTrace();
            statuscode="failed";
        }



    }

        public User[] getUsers() {
            User[] users = null;;
            
            Service  service = new Service();
            Call     call;
            
            try {
                call = (Call) service.createCall();
                call.setTargetEndpointAddress( new java.net.URL(endpoint) );
                
                //call.setOperationName(new QName("urn:EMFUserManager", "getUsers") );
                QName qname1 = new QName("urn:EMFUserManager","ns1:User");
                QName qname2 = new QName("urn:EMFUserManager","ns1:Users");
                QName qname3 = new QName("urn:EMFUserManager", "getUsers");
                
                call.setOperationName(qname3);
                
                Class cls1 = gov.epa.emissions.framework.commons.User.class;
                Class cls2 = gov.epa.emissions.framework.commons.User[].class;
  	          call.registerTypeMapping(cls1, qname1,
  					  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),        
  					  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));        			  
  		          call.registerTypeMapping(cls2, qname2,
  						  new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),        
  						  new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));        			  
  		          
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
     * @see gov.epa.emissions.framework.commons.EMFUserAdmin#updateUsers(gov.epa.emissions.framework.commons.User[])
     */
    public void updateUsers(User[] users) {
        System.out.println("Start transport:updateUsers " + users[0].getFullName());
        System.out.println("Number of elements: " + users.length);
        String statuscode = "default status code";
        Service  service = new Service();
        Call     call;
        
        try {
            call = (Call) service.createCall();
            
            System.out.println("Endpoint: " + endpoint);
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            QName qname1 = new QName("urn:EMFUserManager","ns1:User");
            QName qname2 = new QName("urn:EMFUserManager","ns1:Users");
            QName qname3 = new QName("urn:EMFUserManager", "updateUsers");
                        
            Class cls1 = gov.epa.emissions.framework.commons.User.class;
            Class cls2 = gov.epa.emissions.framework.commons.User[].class;
	        call.registerTypeMapping(cls1, qname1,
					  new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),        
					  new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));  
	        
		    call.registerTypeMapping(cls2, qname2,
						  new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),        
						  new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));        			  
            call.addParameter( "users", qname2, ParameterMode.IN );
            call.setOperationName(qname3);
      
            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
            System.out.println("Before invoke: " + users.length);
           statuscode = (String) call.invoke( new Object[] {users} );
           System.out.println("after invoke: " + users.length);
            System.out.println("Status of updateUsers: " + statuscode);
            
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
        System.out.println("End transport:updateUsers");
        
      
    }

}
