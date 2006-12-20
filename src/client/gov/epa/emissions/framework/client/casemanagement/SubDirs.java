package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.SubDir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubDirs {

    private List list;

    private EmfSession session;

    public SubDirs(EmfSession session, SubDir[] subdirs) {
        this.session = session;
        this.list = new ArrayList(Arrays.asList(subdirs));
    }

    public SubDir get(Object selected) throws EmfException {
        if (selected instanceof String) {
            return editSubdir(selected);
        }
        
        if (selected == null)
            return null;
        
        int index = list.indexOf(selected);
        return (SubDir) list.get(index);
    }

    public SubDir[] getAll() {
        return (SubDir[]) list.toArray(new SubDir[0]);
    }

    private SubDir editSubdir(Object selected) throws EmfException {
        String newSubdir = ((String) selected).trim();
        if (newSubdir.length() == 0)
            return null;

        SubDir name = new SubDir(newSubdir);
        int index = list.indexOf(name);
        if (index == -1) {// new input name
            SubDir persistName = persistName(name);
            list.add(persistName);
            return persistName;
        }
        return (SubDir) list.get(index);
    }

    private SubDir persistName(SubDir name) throws EmfException {
        CaseService service = session.caseService();
        return service.addSubDir(name);
    }
}
