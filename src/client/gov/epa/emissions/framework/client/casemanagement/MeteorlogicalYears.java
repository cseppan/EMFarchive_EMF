package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MeteorlogicalYears {

    private List list;

    public MeteorlogicalYears(MeteorlogicalYear[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public MeteorlogicalYear get(String name) {
        if (name == null)
            return null;

        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            MeteorlogicalYear item = ((MeteorlogicalYear) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new MeteorlogicalYear(name);
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MeteorlogicalYear element = (MeteorlogicalYear) iter.next();
            names.add(element.getName());
        }

        return (String[]) names.toArray(new String[0]);
    }
}
