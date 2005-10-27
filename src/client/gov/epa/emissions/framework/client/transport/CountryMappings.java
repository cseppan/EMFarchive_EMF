package gov.epa.emissions.framework.client.transport;

import javax.xml.namespace.QName;

import gov.epa.emissions.framework.services.Country;

import org.apache.axis.client.Call;

public class CountryMappings {

    private Mapper mapper;

    public CountryMappings() {
        mapper = new Mapper();
    }

    public void register(Call call) {
        mapper.registerBeanMapping(call, Country.class, country());
        mapper.registerArrayMapping(call, Country[].class, countries());
        mapper.registerMappingForTable(call);
    }

    public QName countries() {
        return mapper.qname("ns1:Countries");
    }

    public QName country() {
        return mapper.qname("ns1:Country");
    }

}
