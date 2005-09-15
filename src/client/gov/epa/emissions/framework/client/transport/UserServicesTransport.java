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
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Conrad F. D'Cruz
 * 
 * This class implements the methods specified in the UserAdmin interface
 * 
 */
public class UserServicesTransport implements UserServices {
    private static Log log = LogFactory.getLog(UserServicesTransport.class);

    private String emfSvcsNamespace = EMFConstants.emfServicesNamespace;

    private static String endpoint = "";

    /**
     * 
     */
    public UserServicesTransport() {
        super();
    }

    public UserServicesTransport(String endpt) {
        super();
        endpoint = endpt;
        log.debug("Transport instantiated with endpoint: " + endpt);
    }

    /**
     * 
     * This utility method extracts the significant message from the Axis Fault
     * 
     * @param faultReason
     * @return
     */
    private String extractMessage(String faultReason) {
        log.debug("extracting the faultReason message and parsing");
        String message = faultReason.substring(faultReason.indexOf("Exception: ") + 11);
        if (message.equals("Connection refused: connect")) {
            message = "Cannot communicate with EMF Server";
        }
        log.debug("extracting the faultReason message and parsing. Return message is " + message);
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#authenticate(java.lang.String,
     *      java.lang.String, boolean)
     */
    public void authenticate(String username, String password) throws EmfException {
        log.debug("Authenticate: " + username + " pwd= " + password);
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setMaintainSession(true);
            call.setTargetEndpointAddress(new java.net.URL(endpoint));
            call.setOperationName(new QName(emfSvcsNamespace, "authenticate"));
            QName qname = new QName(emfSvcsNamespace, "ns1:User");
            Class cls = gov.epa.emissions.framework.services.User.class;
            call.registerTypeMapping(cls, qname, BeanSerializerFactory.class, BeanDeserializerFactory.class);

            call.addParameter("username", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);

            call.addParameter("password", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);

            call.invoke(new Object[] { username, password });

        } catch (ServiceException e) {
            log.error(e);
            throw new CommunicationFailureException("Error invoking the service");
        } catch (MalformedURLException e) {
            log.error(e);
            throw new CommunicationFailureException("Error in format of URL string");
        } catch (AxisFault fault) {
            log.error("Axis Fault", fault);
            throw new AuthenticationException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            log.error(e);
            throw new CommunicationFailureException("Error communicating with WS end point");
        }
        log.debug("Authenticate: " + username);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#getUser(java.lang.String)
     */
    public User getUser(String userName) throws CommunicationFailureException {
        log.debug("getting user " + userName);
        User user = null;

        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            QName qname1 = new QName(emfSvcsNamespace, "ns1:User");
            QName qname2 = new QName(emfSvcsNamespace, "getUser");
            call.setOperationName(qname2);
            Class cls = gov.epa.emissions.framework.services.User.class;
            call.registerTypeMapping(User.class, qname1, new org.apache.axis.encoding.ser.BeanSerializerFactory(cls,
                    qname1), new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls, qname1));

            call.addParameter("uname", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(qname1);
            user = (User) call.invoke(new Object[] { userName });

        } catch (ServiceException e) {
            log.error(e);
            throw new CommunicationFailureException("Error invoking the service");
        } catch (MalformedURLException e) {
            log.error(e);
            throw new CommunicationFailureException("Error in format of URL string");
        } catch (RemoteException e) {
            log.error(e);
            throw new CommunicationFailureException("Error communicating with WS end point");
        }
        log.debug("getting user " + userName);
        return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#createUser(gov.epa.emissions.framework.commons.User)
     */
    public void createUser(User newUser) throws CommunicationFailureException, AuthenticationException {
        log.debug("getting user " + newUser.getUsername());
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));
            call.setOperationName(new QName(emfSvcsNamespace, "createUser"));
            QName qname = new QName(emfSvcsNamespace, "ns1:User");
            call.registerTypeMapping(User.class, qname, new BeanSerializerFactory(User.class, qname),
                    new BeanDeserializerFactory(User.class, qname));

            call.addParameter("newuser", qname, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
            call.invoke(new Object[] { newUser });

        } catch (ServiceException e) {
            log.error(e);
            throw new CommunicationFailureException("Error invoking the service");
        } catch (MalformedURLException e) {
            log.error(e);
            throw new CommunicationFailureException("Error in format of URL string");
        } catch (AxisFault fault) {
            log.error("Axis Fault", fault);
            throw new AuthenticationException(extractMessage(fault.getMessage()));
        } catch (RemoteException e) {
            log.error(e);
            throw new CommunicationFailureException("Error communicating with WS end point");
        }

        log.debug("getting user " + newUser.getUsername());
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#updateUser(gov.epa.emissions.framework.commons.User)
     */
    public void updateUser(User newUser) {
        log.debug("Update user record");
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));
            call.setOperationName(new QName(emfSvcsNamespace, "updateUser"));
            QName qname = new QName(emfSvcsNamespace, "ns1:User");
            Class cls = gov.epa.emissions.framework.services.User.class;
            call.registerTypeMapping(cls, qname, new org.apache.axis.encoding.ser.BeanSerializerFactory(cls, qname),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls, qname));

            call.addParameter("updateuser", qname, ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
            call.invoke(new Object[] { newUser });

        } catch (ServiceException e) {
            log.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);
        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);
        }
        log.debug("Update user record");
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#deleteUser(java.lang.String)
     */
    public void deleteUser(String userName) {
        log.debug("Deleting user");

        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));
            call.setOperationName(new QName(emfSvcsNamespace, "deleteUser"));
            call.addParameter("uname", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
            call.invoke(new Object[] { userName });

        } catch (ServiceException e) {
            log.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);
        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);
        }

        log.debug("Deleting user");

    }

    public User[] getUsers() {
        log.debug("Get users");
        User[] users = null;

        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            // call.setOperationName(new QName("urn:EMFUserManager", "getUsers")
            // );
            QName qname1 = new QName(emfSvcsNamespace, "ns1:User");
            QName qname2 = new QName(emfSvcsNamespace, "ns1:Users");
            QName qname3 = new QName(emfSvcsNamespace, "getUsers");

            call.setOperationName(qname3);

            Class cls1 = gov.epa.emissions.framework.services.User.class;
            Class cls2 = gov.epa.emissions.framework.services.User[].class;
            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));
            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));

            call.setReturnType(qname2);

            Object obj = call.invoke(new Object[] {});

            users = (User[]) obj;

        } catch (ServiceException e) {
            log.error("Error invoking the service", e);
        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);
            e.printStackTrace();
        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);
            e.printStackTrace();
        }
        log.debug("Get users");
        return users;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.epa.emissions.framework.commons.EMFUserAdmin#updateUsers(gov.epa.emissions.framework.commons.User[])
     */
    public void updateUsers(User[] users) {
        log.debug("Start transport:updateUsers " + users[0].getFullName());
        log.debug("Number of elements: " + users.length);
        String statuscode = "default status code";
        Service service = new Service();
        Call call;

        try {
            call = (Call) service.createCall();

            log.debug("Endpoint: " + endpoint);
            call.setTargetEndpointAddress(new java.net.URL(endpoint));
            QName qname1 = new QName(emfSvcsNamespace, "ns1:User");
            QName qname2 = new QName(emfSvcsNamespace, "ns1:Users");
            QName qname3 = new QName(emfSvcsNamespace, "updateUsers");

            Class cls1 = gov.epa.emissions.framework.services.User.class;
            Class cls2 = gov.epa.emissions.framework.services.User[].class;
            call.registerTypeMapping(cls1, qname1,
                    new org.apache.axis.encoding.ser.BeanSerializerFactory(cls1, qname1),
                    new org.apache.axis.encoding.ser.BeanDeserializerFactory(cls1, qname1));

            call.registerTypeMapping(cls2, qname2,
                    new org.apache.axis.encoding.ser.ArraySerializerFactory(cls2, qname2),
                    new org.apache.axis.encoding.ser.ArrayDeserializerFactory(qname2));
            call.addParameter("users", qname2, ParameterMode.IN);
            call.setOperationName(qname3);

            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
            log.debug("Before invoke: " + users.length);
            statuscode = (String) call.invoke(new Object[] { users });
            log.debug("after invoke: " + users.length);
            log.debug("Status of updateUsers: " + statuscode);

        } catch (ServiceException e) {
            log.error("Error invoking the service", e);

        } catch (MalformedURLException e) {
            log.error("Error in format of URL string", e);

        } catch (RemoteException e) {
            log.error("Error communicating with WS end point", e);

        }
        log.debug("End transport:updateUsers");

    }

}
