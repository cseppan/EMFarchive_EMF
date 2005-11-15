package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.Page;

import java.net.URL;

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

    public Page getPage(String tableName, int pageNumber) throws EmfException {
        Page page = null;
        
        try {
            log.debug("Is call null? " + (call==null));

            DataEditorMappings mappings = new DataEditorMappings();

            log.debug("Is mappings null? " + (mappings==null));
            mappings.register(call);
            call.setOperationName(mappings.qname("getPage"));
            call.setReturnType(mappings.page());

            call.addParameter("tableName", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
            call.addParameter("pageNumber", org.apache.axis.Constants.XSD_INTEGER, javax.xml.rpc.ParameterMode.IN);

            page = (Page) call.invoke(new Object[] {tableName,new Integer(pageNumber)  });

            call.removeAllParameters();
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get page: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get page: " , e);
        }
        return page;
    }

    public int getPageCount(String tableName) throws EmfException {
        int count = -1;
        
        try {

            call.setOperationName("getPageCount");
            call.addParameter("tableName", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
            call.setReturnType(Constants.XSD_INT);

            Integer cnt = (Integer) call.invoke(new Object[] { tableName });
            count = cnt.intValue();
            call.removeAllParameters();
            
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get count: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get count: " , e);
        }
        return count;
    }

    public Page getPageWithRecord(String tableName, int recordId) throws EmfException {
        Page page = null;
        
        try {
            log.debug("Is call null? " + (call==null));

            DataEditorMappings mappings = new DataEditorMappings();

            log.debug("Is mappings null? " + (mappings==null));
            mappings.register(call);
            call.setOperationName(mappings.qname("getPageWithRecord"));
            call.setReturnType(mappings.page());

            call.addParameter("tableName", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
            call.addParameter("recordId", org.apache.axis.Constants.XSD_INTEGER, javax.xml.rpc.ParameterMode.IN);

            page = (Page) call.invoke(new Object[] {tableName,new Integer(recordId)  });

            call.removeAllParameters();
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get page: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get page: " , e);
        }
        return page;
    }

    public int getTotalRecords(String tableName) throws EmfException {
        int count = -1;
        
        try {

            call.setOperationName("getTotalRecords");
            call.addParameter("tableName", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
            call.setReturnType(Constants.XSD_INT);

            Integer cnt = (Integer) call.invoke(new Object[] { tableName });
            count = cnt.intValue();
            call.removeAllParameters();
            
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to get count: ", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to get count: " , e);
        }
        return count;
    }

    public void close() {
        // TODO Auto-generated method stub
        
    }

}
