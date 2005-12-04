package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.services.EmfDataset;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class ExImMappings extends Mappings {

    public void register(Call call) {
        mapper.registerBeanMapping(call, EmfDataset.class, dataset());
        mapper.registerArrayMapping(call, EmfDataset[].class, datasets());
        mapper.registerBeanMapping(call, DatasetType.class, qname("DatasetType"));

        mapper.registerMappingForTable(call);
        mapper.registerBeanMapping(call, InternalSource.class, qname("InternalSource"));
        mapper.registerBeanMapping(call, ExternalSource.class, qname("ExternalSource"));
        mapper.registerArrayMapping(call, ExternalSource[].class, qname("ExternalSources"));
        mapper.registerArrayMapping(call, InternalSource[].class, qname("InternalSources"));
        mapper.registerBeanMapping(call, KeyVal.class, qname("KeyVal"));
        mapper.registerArrayMapping(call, KeyVal[].class, qname("KeyVals"));
        mapper.registerBeanMapping(call, Keyword.class, qname("Keyword"));
        mapper.registerArrayMapping(call, Keyword[].class, qname("Keywords"));
    }

    public QName qname(String name) {
        return qname(name);
    }

    public QName dataset() {
        return qname("EmfDataset");
    }

    public QName datasets() {
        return qname("EmfDatasets");
    }

}
