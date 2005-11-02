package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.Keyword;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class KeywordMappings {

    private Mapper mapper;

    public KeywordMappings() {
        mapper = new Mapper();
    }

    public void register(Call call) {
        mapper.registerBeanMapping(call, Keyword.class, keyword());
        mapper.registerArrayMapping(call, Keyword[].class, keywords());
        mapper.registerMappingForTable(call);
    }

    public QName keywords() {
        return mapper.qname("ns1:Keywords");
    }

    public QName keyword() {
        return mapper.qname("ns1:Keyword");
    }

    public QName qname(String name) {
        return mapper.qname(name);
    }

}
