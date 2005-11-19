package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.User;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class ExImServiceMappings extends Mappings {

    public void register(Call call) {
        bean(call, EmfDataset.class, "EmfDataset");
        bean(call, DatasetType.class, "DatasetType");
        bean(call, InternalSource.class, "InternalSource");
        bean(call, User.class, "User");
        bean(call, ExternalSource.class, "ExternalSource");
        bean(call, Keyword.class, "Keyword");
        bean(call, KeyVal.class, "KeyVal");

        array(call, EmfDataset[].class, "EmfDatasets");
        array(call, ExternalSource[].class, "ExternalSources");
        array(call, InternalSource[].class, "InternalSources");
        array(call, Keyword[].class, "Keywords");
        array(call, KeyVal[].class, "KeyVals");

        registerTable(call);
    }

    public QName user() {
        return qname("User");
    }

    public QName datasets() {
        return qname("EmfDatasets");
    }

    public QName datasetType() {
        return qname("DatasetType");
    }

}
