package gov.epa.emissions.framework.client.transport;

import javax.xml.namespace.QName;

import gov.epa.emissions.framework.services.Country;

import org.apache.axis.client.Call;

public class CountryMappings extends Mappings {

    public void register(Call call) {
        bean(call, Country.class, "Country");
        array(call, Country[].class, "Countries");
        registerTable(call);
    }

    public QName countries() {
        return qname("Countries");
    }

    public QName country() {
        return qname("Country");
    }

}
