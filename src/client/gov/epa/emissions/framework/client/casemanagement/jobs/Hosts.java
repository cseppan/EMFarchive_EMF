package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.services.casemanagement.jobs.Host;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hosts {

    private List<Host> list;

    public Hosts(Host[] hosts) {
        this.list = new ArrayList<Host>(Arrays.asList(hosts));
    }

    public Host get(Object selected) {
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return list.get(index);
    }

    public Host[] getAll() {
        return list.toArray(new Host[0]);
    }

}
