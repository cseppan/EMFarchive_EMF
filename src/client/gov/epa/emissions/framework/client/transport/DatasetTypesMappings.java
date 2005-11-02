package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class DatasetTypesMappings {

    private Mapper mapper;

    public DatasetTypesMappings() {
        mapper = new Mapper();
    }

    public void register(Call call) {
        mapper.registerBeanMapping(call, DatasetType.class, datasetType());
        mapper.registerArrayMapping(call, DatasetType[].class, datasetTypes());
        mapper.registerBeanMapping(call, Keyword.class, qname("ns1:Keyword"));
        mapper.registerArrayMapping(call, Keyword[].class, qname("ns1:Keywords"));
    }

    public QName datasetType() {
        return qname("ns1:DatasetType");
    }

    public QName datasetTypes() {
        return qname("ns1:DatasetTypes");
    }

    public QName qname(String name) {
        return mapper.qname(name);
    }

}
