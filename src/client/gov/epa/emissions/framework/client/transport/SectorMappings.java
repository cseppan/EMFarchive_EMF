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
        mapper.registerBeanMapping(call, Sector.class, mapper.qname("ns1:Sector"));
        mapper.registerArrayMapping(call, Sector[].class, mapper.qname("ns1:Sectors"));
        mapper.registerBeanMapping(call, SectorCriteria.class, mapper.qname("ns1:SectorCriteria"));
        mapper.registerArrayMapping(call, SectorCriteria[].class, mapper.qname("ns1:SectorCriterias"));
        mapper.registerMappingForTable(call);
    }

    public QName sector() {
        return mapper.qname("ns1:Sector");
    }

    public QName sectors() {
        return mapper.qname("ns1:Sectors");
    }

}
