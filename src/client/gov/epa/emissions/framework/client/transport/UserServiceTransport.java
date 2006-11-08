package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;

public class UserServiceTransport implements UserService {
    private CallFactory callFactory;

    private DataMappings mappings;

    public UserServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    public void authenticate(String username, String password) throws EmfException {
        EmfCall call = call();

        call.setOperation("authenticate");
        call.addStringParam("username");
        call.addStringParam("password");
        call.setStringReturnType();

        call.request(new Object[] { username, password });
    }

    public User getUser(String username) throws EmfException {
        EmfCall call = call();

        call.setOperation("getUser");
        call.addStringParam("username");
        call.setReturnType(mappings.user());
        Object[] params = new Object[] { username };

        return (User) call.requestResponse(params);
    }

    public User createUser(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("createUser");
        call.addParam("user", mappings.user());
        call.setReturnType(mappings.user());

        return (User) call.requestResponse(new Object[] { user });
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("User Service");
    }

    public void updateUser(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateUser");
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { user });
    }

    public void deleteUser(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("deleteUser");
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { user });
    }

    public User[] getUsers() throws EmfException {
        EmfCall call = call();

        call.setOperation("getUsers");
        call.setReturnType(mappings.users());

        return (User[]) call.requestResponse(new Object[0]);
    }

    public User obtainLocked(User owner, User object) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", mappings.user());
        call.addParam("object", mappings.user());
        call.setReturnType(mappings.user());
        Object[] params = new Object[] { owner, object };

        return (User) call.requestResponse(params);
    }

    public User releaseLocked(User object) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addParam("object", mappings.user());
        call.setReturnType(mappings.user());

        return (User) call.requestResponse(new Object[] { object });
    }

}
