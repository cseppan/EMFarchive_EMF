package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.Page;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataEditorServicesTransport implements DataEditorServices {
    private static Log log = LogFactory.getLog(DataEditorServicesTransport.class);

    private Call call = null;
    private String emfSvcsNamespace = EMFConstants.emfServicesNamespace;

    public DataEditorServicesTransport(String endPoint, Call call) {
         try {
            log.debug("Constructor: DataEditorServicesTransport");
            this.call = call;
            call.setTargetEndpointAddress(new URL(endPoint));
        } catch (Exception e) {
            log.error("Transport Error: " + e.getMessage());
        }
        log.debug("Constructor complete");
    }

    public String getName() throws Exception {
       String name = null;
       
        try {

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
            call.setOperationName("setName");
            call.addParameter("name", org.apache.axis.Constants.XSD_STRING, ParameterMode.IN);
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] {name});
            call.removeAllParameters();

        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get name: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get name: " , e);
        }
        
    }

    private String extractMessage(String faultReason) {
        return faultReason.substring(faultReason.indexOf("Exception: ") + 11);
    }


    private void throwExceptionDueToServiceErrors(String message, Exception e) throws EmfException {
        log.error(message, e);
        throw new EmfException(message, e.getMessage(), e);
    }

    private void throwExceptionOnAxisFault(String message, AxisFault fault) throws EmfException {
        log.error(message, fault);
        throw new EmfException(extractMessage(fault.getMessage()));
    }

    public Page getPage(String tableName, int pageNumber) throws Exception {
        Page page = null;
        
        try {
            call.setOperationName("getPage");
            call.setReturnType(Constants.XSD_ANY);

            QName pageQname = new QName(emfSvcsNamespace, "ns1:Page");
            call.addParameter("tableName", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
            call.addParameter("pageNumber", org.apache.axis.Constants.XSD_INTEGER, javax.xml.rpc.ParameterMode.IN);

            call.setReturnType(pageQname);

            page = (Page) call.invoke(new Object[] {tableName,new Integer(pageNumber)  });

            call.removeAllParameters();
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get page: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get page: " , e);
        }
        return page;
    }

    public int getPageCount(String tableName) throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }

}
