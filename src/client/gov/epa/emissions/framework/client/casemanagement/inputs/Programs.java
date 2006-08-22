package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.Program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Programs {

    private List list;

    private EmfSession session;

    public Programs(EmfSession session, Program[] pgrograms) {
        this.session = session;
        this.list = new ArrayList(Arrays.asList(pgrograms));
    }

    public Program get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return editProgramType(selected);
        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return (Program) list.get(index);
    }

    public Program[] getAll() {
        return (Program[]) list.toArray(new Program[0]);
    }

    private Program editProgramType(Object selected) throws EmfException {
        String newProgram = ((String) selected).trim();
        if (newProgram.length() == 0)
            return null;

        Program name = new Program(newProgram);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            Program persistName = persistName(name);
            list.add(persistName);
            return persistName;
        }
        return (Program) list.get(index);
    }

    private Program persistName(Program name) throws EmfException {
        CaseService service = session.caseService();
        return service.addProgram(name);
    }
}
