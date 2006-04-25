package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.Grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Grids {

    private List list;

    public Grids(Grid[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public Grid get(String name) {
        if (name == null)
            return null;

        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            Grid item = ((Grid) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new Grid(name);
    }

    public String[] names() {
        List names = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Grid element = (Grid) iter.next();
            names.add(element.getName());
        }

        return (String[]) names.toArray(new String[0]);
    }
}
