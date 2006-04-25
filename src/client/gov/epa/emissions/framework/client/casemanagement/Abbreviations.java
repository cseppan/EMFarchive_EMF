package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.Abbreviation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Abbreviations {

    private List list;

    public Abbreviations(Abbreviation[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public Abbreviation get(String name) {
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            Abbreviation item = ((Abbreviation) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new Abbreviation(name);
    }

    public Abbreviation[] all() {
        return (Abbreviation[]) list.toArray(new Abbreviation[0]);
    }

}
