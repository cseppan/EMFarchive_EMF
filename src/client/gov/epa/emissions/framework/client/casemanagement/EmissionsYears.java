package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EmissionsYears {

    private List list;

    public EmissionsYears(EmissionsYear[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public EmissionsYear get(String name) {
        if(name == null || name.trim().length() == 0)
            return null;

        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            EmissionsYear item = ((EmissionsYear) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new EmissionsYear(name);
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            EmissionsYear element = (EmissionsYear) iter.next();
            names.add(element.getName());
        }

        return (String[]) names.toArray(new String[0]);
    }
}
