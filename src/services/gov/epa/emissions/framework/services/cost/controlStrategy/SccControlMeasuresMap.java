package gov.epa.emissions.framework.services.cost.controlStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SccControlMeasuresMap {

    private Map map;

    public SccControlMeasuresMap() {
        map = new HashMap();
    }

    public void add(String scc, int controlMeasureId) {
        List list = (List) map.get(scc);
        if (list == null) {
            list = new ArrayList();
            map.put(scc, list);
        }
        list.add(new Integer(controlMeasureId));
    }

    public int[] getControlMeasureIds(String scc) {
        List list = (List) map.get(scc);
        if (list == null)
            list = Collections.EMPTY_LIST;
        return ids(list);
    }

    private int[] ids(List list) {
        int[] ids = new int[list.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = ((Integer) list.get(i)).intValue();
        }
        return ids;
    }

    public int size() {
        return map.size();
    }
}
