package gov.epa.emissions.framework.client.transport;

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

}
