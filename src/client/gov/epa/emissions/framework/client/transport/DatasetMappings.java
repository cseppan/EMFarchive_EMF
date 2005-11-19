package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.services.EmfDataset;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class DatasetMappings extends Mappings {

    public void register(Call call) {
        bean(call, EmfDataset.class, "EmfDataset");
        bean(call, DatasetType.class, "DatasetType");
        bean(call, InternalSource.class, "InternalSource");
        bean(call, ExternalSource.class, "ExternalSource");
        bean(call, KeyVal.class, "KeyVal");
        bean(call, Keyword.class, "Keyword");

        array(call, EmfDataset[].class, "EmfDatasets");
        array(call, ExternalSource[].class, "ExternalSources");
        array(call, InternalSource[].class, "InternalSources");
        array(call, KeyVal[].class, "KeyVals");
        array(call, Keyword[].class, "Keywords");

        registerTable(call);
    }

    public QName dataset() {
        return qname("EmfDataset");
    }

    public QName datasets() {
        return qname("EmfDatasets");
    }

}
