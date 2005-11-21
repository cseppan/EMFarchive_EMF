package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.AccessLog;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class LoggingMappings extends Mappings {

    public void register(Call call) {
        bean(call, AccessLog.class, "AccessLog");
        array(call, AccessLog[].class, "AllAccessLogs");
    }

    public QName logs() {
        return qname("AllAccessLogs");
    }

    public QName log() {
        return qname("AccessLog");
    }

}
