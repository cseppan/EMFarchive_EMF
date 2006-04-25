package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.CaseCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CaseCategories {

    private List list;

    public CaseCategories(CaseCategory[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public CaseCategory get(String name) {
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            CaseCategory item = ((CaseCategory) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new CaseCategory(name);
    }

}
