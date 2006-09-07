package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Programs {

    private List list;

    private EmfSession session;

    public Programs(EmfSession session, CaseProgram[] pgrograms) {
        this.session = session;
        this.list = new ArrayList(Arrays.asList(pgrograms));
    }

    public CaseProgram get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return editProgramType(selected);
        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return (CaseProgram) list.get(index);
    }

    public CaseProgram[] getAll() {
        return (CaseProgram[]) list.toArray(new CaseProgram[0]);
    }

    private CaseProgram editProgramType(Object selected) throws EmfException {
        String newProgram = ((String) selected).trim();
        if (newProgram.length() == 0)
            return null;

        CaseProgram name = new CaseProgram(newProgram);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            CaseProgram persistName = persistName(name);
            list.add(persistName);
            return persistName;
        }
        return (CaseProgram) list.get(index);
    }

    private CaseProgram persistName(CaseProgram name) throws EmfException {
        CaseService service = session.caseService();
        return service.addProgram(name);
    }
}
