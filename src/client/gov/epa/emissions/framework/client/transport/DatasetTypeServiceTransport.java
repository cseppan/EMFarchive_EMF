package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypeService;

import java.net.URL;

import javax.xml.rpc.ParameterMode;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DatasetTypeServiceTransport implements DatasetTypeService {
    private static Log LOG = LogFactory.getLog(DatasetTypeServiceTransport.class);

    private String endpoint;

    private EmfMappings mappings;

    public DatasetTypeServiceTransport(String endpt) {
        endpoint = endpt;
        mappings = new EmfMappings();
    }

    public DatasetType[] getDatasetTypes() throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            call.setOperationName(mappings.qname("getDatasetTypes"));
            call.setReturnType(mappings.datasetTypes());

            return (DatasetType[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get all the DatasetTypes", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get all the DatasetTypes", e);
        }

        return null;
    }

    public void updateDatasetType(DatasetType datasetType) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            call.setOperationName(mappings.qname("updateDatasetType"));
            call.addParameter("datasettype", mappings.datasetType(), ParameterMode.IN);
            call.setReturnType(Constants.XSD_ANY);

            call.invoke(new Object[] { datasetType });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not update DatasetType: " + datasetType.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not update DatasetType: " + datasetType.getName(), e);
        }

    }

    public void insertDatasetType(DatasetType datasetType) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            call.setOperationName(mappings.qname("insertDatasetType"));
            call.addParameter("datasettype", mappings.datasetType(), ParameterMode.IN);
            call.setReturnType(org.apache.axis.Constants.XSD_ANY);

            call.invoke(new Object[] { datasetType });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not add DatasetType: " + datasetType.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not add DatasetType: " + datasetType.getName(), e);
        }
    }

    private Call call() throws Exception {
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(new URL(endpoint));

        return call;
    }

    private String extractMessage(String faultReason) {
        return faultReason.substring(faultReason.indexOf("Exception: ") + 11);
    }

    private void throwExceptionDueToServiceErrors(String message, Exception e) throws EmfException {
        LOG.error(message, e);
        throw new EmfException(message, e.getMessage(), e);
    }

    private void throwExceptionOnAxisFault(String message, AxisFault fault) throws EmfException {
        LOG.error(message, fault);
        throw new EmfException(extractMessage(fault.getMessage()));
    }

}
