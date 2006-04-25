package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Regions {

    private List list;

    public Regions(Region[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    // FIXME: why we are creating new item?
    public Region get(String name) {
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            Region item = ((Region) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new Region(name);
    }

}
