package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Projects {

    private List list;

    public Projects(Project[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public Project get(String name) {
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            Project item = ((Project) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new Project(name);
    }

    public Project[] all() {
        return (Project[]) list.toArray(new Project[0]);
    }
}
