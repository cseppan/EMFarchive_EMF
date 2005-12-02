package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class DataEditorMappings extends Mappings {

    private Mapper mapper;

    public DataEditorMappings() {
        mapper = new Mapper();
    }

    public void register(Call call) {
        mapper.registerBeanMapping(call, Page.class, page());
        mapper.registerArrayMapping(call, Page[].class, pages());
        mapper.registerBeanMapping(call, VersionedRecord.class, record());
        mapper.registerArrayMapping(call, VersionedRecord[].class, records());

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
