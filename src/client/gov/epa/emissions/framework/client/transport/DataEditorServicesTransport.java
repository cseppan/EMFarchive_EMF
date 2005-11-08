package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorServices;

import java.net.URL;

import javax.xml.rpc.ParameterMode;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataEditorServicesTransport implements DataEditorServices {
    private static Log log = LogFactory.getLog(DataEditorServicesTransport.class);

    private String endpoint;
    private Call call = null;
    
    public DataEditorServicesTransport(String endPoint, Call call) {
        endpoint = endPoint;
         try {
            log.debug("Constructor: DataEditorServicesTransport");
            this.call = call;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() throws Exception {
       String name = null;
       
        try {
            call.setTargetEndpointAddress(new URL(endpoint));

            call.setOperationName("getName");
            call.setReturnType(Constants.XSD_ANY);

            name = (String) call.invoke(new Object[] {  });
            call.removeAllParameters();
            //call.removeProperty();
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get name: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get name: " , e);
        }
        return name;
    }


    public void setName(String name) throws Exception {
        try {
            call.setTargetEndpointAddress(new URL(endpoint));

            call.setOperationName("setName");
            call.addParameter("name", org.apache.axis.Constants.XSD_STRING, ParameterMode.IN);
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] {name});
            call.removeAllParameters();
//            call.removeProperty();

        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get name: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get name: " , e);
        }
        
    }


    private String extractMessage(String faultReason) {
        return faultReason.substring(faultReason.indexOf("Exception: ") + 11);
    }

    private Call call() throws Exception {
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(new URL(endpoint));

        return call;
    }

    private void throwExceptionDueToServiceErrors(String message, Exception e) throws EmfException {
        log.error(message, e);
        throw new EmfException(message, e.getMessage(), e);
    }

    private void throwExceptionOnAxisFault(String message, AxisFault fault) throws EmfException {
        log.error(message, fault);
        throw new EmfException(extractMessage(fault.getMessage()));
    }

}
