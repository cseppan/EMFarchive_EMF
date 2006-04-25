package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeteorlogicalYears {

    private List list;

    public MeteorlogicalYears(MeteorlogicalYear[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public MeteorlogicalYear get(String name) {
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            MeteorlogicalYear item = ((MeteorlogicalYear) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new MeteorlogicalYear(name);
    }
    public MeteorlogicalYear[] all() {
        return (MeteorlogicalYear[]) list.toArray(new MeteorlogicalYear[0]);
    }
}
