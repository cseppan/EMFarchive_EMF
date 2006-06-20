package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.framework.services.cost.ControlMeasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SCCControlMeasureMap {
   
    private Map map;

    public SCCControlMeasureMap(String[] sccs, ControlMeasure[] measures) {
        this.map = new HashMap();
        
        setMaps(sccs, measures);
    }
    
    private void setMaps(String[] sccs, ControlMeasure[] measures) {
        for (int i = 0; i < sccs.length; i++)
            map.put(sccs[i], getControlMeasuresList(sccs[i], measures));
    }

    private List getControlMeasuresList(String scc, ControlMeasure[] measures) {
        List measuresList = new ArrayList();
        for (int i = 0; i < measures.length; i++) {
            if (contains(measures[i], scc))
                measuresList.add(measures[i]);
        }
        
        return measuresList;
    }

    private boolean contains(ControlMeasure measure, String scc) {
        String[] sccs = measure.getSccs();
        
        for (int i = 0; i < sccs.length; i++)
            if (scc.equalsIgnoreCase(sccs[i]))
                return true;
        
        return false;
    }
    
    public List getMappedControlMeasuresList(String scc) {
        return (List) map.get(scc);
    }
    
}
