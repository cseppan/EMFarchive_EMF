package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.GridResolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GridResolutions {
    private List list;

    private EmfSession session;

    public GridResolutions(EmfSession session, GridResolution[] resolutions) {
        this.session = session;
        this.list = new ArrayList(Arrays.asList(resolutions));
    }

    public GridResolution get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return editGridResolutionType(selected);
        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return (GridResolution) list.get(index);
    }

    public GridResolution[] getAll() {
        return (GridResolution[]) list.toArray(new GridResolution[0]);
    }

    private GridResolution editGridResolutionType(Object selected) throws EmfException {
        String newresolution = ((String) selected).trim();
        if (newresolution.length() == 0)
            return null;

        GridResolution name = new GridResolution(newresolution);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            GridResolution persistName = persistName(name);
            list.add(persistName);
            return persistName;
        }
        return (GridResolution) list.get(index);
    }

    private GridResolution persistName(GridResolution resolution) throws EmfException {
        CaseService service = session.caseService();
        return service.addGridResolution(resolution);
    }
}
