package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChangeSets {

    private List list;

    public ChangeSets() {
        this.list = new ArrayList();
    }

    public ChangeSets(List list) {
        this.list = list;
    }

    public void add(ChangeSet set) {
        list.add(set);
    }

    public int size() {
        return list.size();
    }

    public int netIncrease() {
        int result = 0;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            ChangeSet element = (ChangeSet) iter.next();
            result += element.netIncrease();
        }

        return result;
    }

    public void add(ChangeSets another) {
        for (Iterator iter = another.iterator(); iter.hasNext();)
            add((ChangeSet) iter.next());
    }

    public Iterator iterator() {
        return list.iterator();
    }

    public ChangeSet get(int index) {
        return (ChangeSet) list.get(index);
    }

}
