package gov.epa.emissions.framework.client.casemanagement;

import java.util.ArrayList;
import java.util.List;

public class RunStatuses {

    private List list;

    public RunStatuses() {
        this.list = new ArrayList();

        list.add("Not Started");
        list.add("Running");
        list.add("Failed");
        list.add("Complete");
    }

    public String[] all() {
        return (String[]) list.toArray(new String[0]);
    }
}
