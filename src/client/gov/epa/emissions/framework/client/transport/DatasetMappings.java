package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.EmfDataset;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class DatasetMappings {

    private Mapper mapper;

    public DatasetMappings() {
        mapper = new Mapper();
    }

    public void register(Call call) {

        mapper.registerBeanMapping(call, EmfDataset.class, dataset());
        mapper.registerArrayMapping(call, EmfDataset[].class, datasets());
        mapper.registerBeanMapping(call, DatasetType.class, mapper.qname("ns1:DatasetType"));

        mapper.registerMappingForTable(call);
        mapper.registerBeanMapping(call, InternalSource.class, mapper.qname("ns1:InternalSource"));
        mapper.registerBeanMapping(call, ExternalSource.class, mapper.qname("ns1:ExternalSource"));
        mapper.registerArrayMapping(call, ExternalSource[].class, mapper.qname("ns1:ExternalSources"));
        mapper.registerArrayMapping(call, InternalSource[].class, mapper.qname("ns1:InternalSources"));
    }

    public QName dataset() {
        return mapper.qname("ns1:EmfDataset");
    }

    public QName datasets() {
        return mapper.qname("ns1:EmfDatasets");
    }

}
