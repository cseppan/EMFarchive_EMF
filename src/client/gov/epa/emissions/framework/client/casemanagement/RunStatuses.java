package gov.epa.emissions.framework.client.casemanagement;

import java.util.ArrayList;
import java.util.List;

public class RunStatuses {
 
    private static String [] statusList;
    
    static
    {
        List list = new ArrayList();

        list.add("Not Started");
        list.add("Running");
        list.add("Failed");
        list.add("Complete");
        
        statusList = new String[4];
        list.toArray(statusList);
    }
        
    public static String[] all() {
        return statusList;
    }
}
