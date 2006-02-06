package gov.epa.emissions.framework.client.transport;

import org.apache.axis.AxisFault;

public class EmfServiceFault {

    private AxisFault fault;

    public static final String CONNECTION_REFUSED = "Connection refused: connect";

    public EmfServiceFault(AxisFault fault) {
        this.fault = fault;
    }

    public String message() {
        Throwable cause = fault.getCause();
        if ((cause != null) && (CONNECTION_REFUSED.equals(cause.getMessage())))
            return "EMF server not responding";

        String faultReason = fault.getFaultReason();
        return faultReason.substring(faultReason.indexOf("Exception: ") + 11);
    }

}
