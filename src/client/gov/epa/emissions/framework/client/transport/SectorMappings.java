package gov.epa.emissions.framework.client.transport;

import javax.xml.namespace.QName;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.io.SectorCriteria;

import org.apache.axis.client.Call;

public class SectorMappings {

    private Mapper mapper;

    public SectorMappings() {
        mapper = new Mapper();
    }

    public void register(Call call) {
        mapper.registerBeanMapping(call, Sector.class, sector());
        mapper.registerArrayMapping(call, Sector[].class, sectors());
        mapper.registerBeanMapping(call, SectorCriteria.class, qname("ns1:SectorCriteria"));
        mapper.registerArrayMapping(call, SectorCriteria[].class, qname("ns1:SectorCriterias"));
        mapper.registerMappingForTable(call);
    }

    public QName qname(String name) {
        return mapper.qname(name);
    }

    public QName sector() {
        return qname("ns1:Sector");
    }

    public QName sectors() {
        return qname("ns1:Sectors");
    }

}
