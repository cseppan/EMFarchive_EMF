package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.Page;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class DataEditorMappings {

    private Mapper mapper;

    public DataEditorMappings() {
        mapper = new Mapper();
    }

    public void register(Call call) {
        mapper.registerBeanMapping(call, Page.class, page());
        mapper.registerArrayMapping(call, Page[].class, pages());
        mapper.registerBeanMapping(call, Record.class, record());
        mapper.registerArrayMapping(call, Record[].class, records());
        
        mapper.registerMappingForTable(call);
    }

    public QName qname(String name) {
        return mapper.qname(name);
    }

    public QName page() {
        return mapper.qname("ns1:Page");
    }

    public QName record() {
        return mapper.qname("ns1:Record");
    }

    public QName pages() {
        return mapper.qname("ns1:Pages");
    }

    public QName records() {
        return mapper.qname("ns1:Records");
    }

}
