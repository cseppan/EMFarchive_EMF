package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.UserService;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserServiceTransport implements UserService {
    private static Log LOG = LogFactory.getLog(UserServiceTransport.class);

    private CallFactory callFactory;

    private EmfMappings mappings;

    public UserServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    public void authenticate(String username, String password) throws EmfException {
        try {
            Call call = callFactory.createCall();
            call.setMaintainSession(true);

            mappings.register(call);
            mappings.setOperation(call, "authenticate");
            mappings.addStringParam(call, "username");
            mappings.addStringParam(call, "password");
            mappings.setStringReturnType(call);

            call.invoke(new Object[] { username, password });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not authenticate user: " + username, fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not authenticate user: " + username, e);
        }
    }

    public User getUser(String username) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "getUser");
            mappings.addStringParam(call, "username");
            mappings.setReturnType(call, mappings.user());

            return (User) call.invoke(new Object[] { username });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get user: " + username, fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get user: " + username, e);
        }

        return null;
    }

    public void createUser(User user) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "createUser");
            mappings.addParam(call, "user", mappings.user());
            mappings.setStringReturnType(call);

            call.invoke(new Object[] { user });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not create user: " + user.getUsername(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not create user: " + user.getUsername(), e);
        }
    }

    public void updateUser(User user) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "updateUser");
            mappings.addParam(call, "user", mappings.user());
            mappings.setVoidReturnType(call);

            call.invoke(new Object[] { user });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not update user: " + user.getUsername(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not update user: " + user.getUsername(), e);
        }
    }

    public void deleteUser(User user) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "deleteUser");
            mappings.addParam(call, "user", mappings.user());
            mappings.setVoidReturnType(call);

            call.invoke(new Object[] { user });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not delete user: " + user.getUsername(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not delete user: " + user.getUsername(), e);
        }
    }

    public User[] getUsers() throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "getUsers");
            mappings.setReturnType(call, mappings.users());

            return (User[]) call.invoke(new Object[0]);
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get all users", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get all users", e);
        }

        return null;
    }

    public User obtainLocked(User owner, User object) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "obtainLocked");
            mappings.addParam(call, "owner", mappings.user());
            mappings.addParam(call, "object", mappings.user());
            call.setReturnType(mappings.user());

            return (User) call.invoke(new Object[] { owner, object });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get User lock: " + object.getUsername(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get User lock: " + object.getUsername(), e);
        }

        return null;
    }

    public User releaseLocked(User object) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "releaseLocked");
            mappings.addParam(call, "object", mappings.user());
            call.setReturnType(mappings.user());

            return (User) call.invoke(new Object[] { object });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not release User lock: " + object.getUsername(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not release User lock: " + object.getUsername(), e);
        }

        return null;
    }

    private String extractMessage(String faultReason) {
        return faultReason.substring(faultReason.indexOf("Exception: ") + 11);
    }

    private void throwExceptionDueToServiceErrors(String message, Exception e) throws EmfException {
        LOG.error(message, e);
        throw new EmfException(message, e.getMessage(), e);
    }

    private void throwExceptionOnAxisFault(String message, AxisFault fault) throws EmfException {
        LOG.error(message, fault);
        String msg=extractMessage(fault.getMessage());
        
        if (fault.getCause()!=null){
            if (fault.getCause().getMessage().equals(EMFConstants.CONNECTION_REFUSED)){
                msg="EMF server not responding";
            }            
        }
        throw new EmfException(msg);
    }

}
