package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.Speciation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Speciations {

    private List list;

    public Speciations(Speciation[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public Speciation get(String name) {
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            Speciation item = ((Speciation) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new Speciation(name);
    }

    public Speciation[] all() {
        return (Speciation[]) list.toArray(new Speciation[0]);
    }
}
