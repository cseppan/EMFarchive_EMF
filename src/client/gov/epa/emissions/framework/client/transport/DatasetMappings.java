package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
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
        mapper.registerBeanMapping(call, DatasetType.class, qname("ns1:DatasetType"));

        mapper.registerMappingForTable(call);
        mapper.registerBeanMapping(call, InternalSource.class, qname("ns1:InternalSource"));
        mapper.registerBeanMapping(call, ExternalSource.class, qname("ns1:ExternalSource"));
        mapper.registerArrayMapping(call, ExternalSource[].class, qname("ns1:ExternalSources"));
        mapper.registerArrayMapping(call, InternalSource[].class, qname("ns1:InternalSources"));
        mapper.registerBeanMapping(call, KeyVal.class, qname("ns1:KeyVal"));
        mapper.registerArrayMapping(call, KeyVal[].class, qname("ns1:KeyVals"));
        mapper.registerBeanMapping(call, Keyword.class, qname("ns1:Keyword"));
        mapper.registerArrayMapping(call, Keyword[].class, qname("ns1:Keywords"));
    }

    public QName qname(String name) {
        return mapper.qname(name);
    }

    public QName dataset() {
        return mapper.qname("ns1:EmfDataset");
    }

    public QName datasets() {
        return mapper.qname("ns1:EmfDatasets");
    }

}
