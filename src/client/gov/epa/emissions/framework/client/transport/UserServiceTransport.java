package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.UserService;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;

public class UserServiceTransport implements UserService {
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to User Service");
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to User Service");
        }
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to User Service");
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to User Service");
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to User Service");
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to User Service");
        }
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to User Service");
        }
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to User Service");
        }
    }

}
