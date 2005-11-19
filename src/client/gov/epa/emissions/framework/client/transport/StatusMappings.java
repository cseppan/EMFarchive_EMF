package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.Status;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class StatusMappings extends Mappings {

    public void register(Call call) {
        bean(call, Status.class, "Status");
        array(call, Status[].class, "AllStatus");
    }

    public QName statuses() {
        return qname("AllStatus");
    }

    public QName status() {
        return qname("Status");
    }

}
