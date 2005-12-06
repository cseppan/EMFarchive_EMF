package gov.epa.emissions.framework.client.transport;

import javax.xml.namespace.QName;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.io.SectorCriteria;

import org.apache.axis.client.Call;

public class SectorMappings extends Mappings {

    public void register(Call call) {
        bean(call, Sector.class, "Sector");
        array(call, Sector[].class, "Sectors");
        
        bean(call, SectorCriteria.class, "SectorCriteria");
        array(call, SectorCriteria[].class, "SectorCriterias");
        
        registerTable(call);
    }

    public QName sector() {
        return qname("Sector");
    }

    public QName sectors() {
        return qname("Sectors");
    }

}
