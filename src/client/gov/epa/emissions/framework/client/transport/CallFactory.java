package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;

import java.net.URL;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class CallFactory {

    private String endpoint;

    public CallFactory(String endpoint) {
        this.endpoint = endpoint;
    }

    public Call createCall() throws Exception {
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(new URL(endpoint));

        return call;
    }

    public EmfCall createEmfCall(String service) throws EmfException {
        try {
            return new EmfCall(createCall(), service);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to " + service);
        }
    }

    public EmfCall createSessionEnabledCall(String service) throws EmfException {
        EmfCall call = createEmfCall(service);
        call.enableSession();

        return call;
    }

}
