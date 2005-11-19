package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.Keyword;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class KeywordMappings extends Mappings {

    public void register(Call call) {
        bean(call, Keyword.class, "Keyword");
        array(call, Keyword[].class, "Keywords");
        registerTable(call);
    }

    public QName keywords() {
        return qname("Keywords");
    }

    public QName keyword() {
        return qname("Keyword");
    }

}
