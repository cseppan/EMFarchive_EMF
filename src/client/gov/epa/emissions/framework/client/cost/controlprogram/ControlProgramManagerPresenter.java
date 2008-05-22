package gov.epa.emissions.framework.client.cost.controlprogram;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlprogram.editor.EditControlProgramPresenter;
import gov.epa.emissions.framework.client.cost.controlprogram.editor.EditControlProgramPresenterImpl;
import gov.epa.emissions.framework.client.cost.controlprogram.editor.EditControlProgramView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramService;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class ControlProgramManagerPresenter implements RefreshObserver {
    private ControlProgramManagerView view;

    private EmfSession session;

    private LightControlMeasure[] controlMeasures = {};

    public ControlProgramManagerPresenter(EmfSession session, ControlProgramManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {
        view.display(service().getControlPrograms());
        view.observe(this);
    }

    private ControlProgramService service() {
        return session.controlProgramService();
    }

    public void doRefresh() throws EmfException {
//        loadControlMeasures();
        view.refresh(service().getControlPrograms());
    }

    public void doClose() {
        view.disposeView();
    }

//    public void doNew(ControlProgramView view) {
//        ControlProgramPresenter presenter = new ControlProgramPresenterImpl(session, view, this);
//        presenter.doDisplay();
//        
//    }
//
    public void doEdit(EditControlProgramView view, ControlProgram controlProgram) throws EmfException {
        EditControlProgramPresenter presenter = new EditControlProgramPresenterImpl(controlProgram, session, view, this);
        displayEditor(presenter);
    }

    void displayEditor(EditControlProgramPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doRemove(int[] ids) throws EmfException {
        service().removeControlPrograms(ids, session.user());
    }

//    public void doSaveCopiedStrategies(ControlProgram coppied, String name) throws EmfException {
//        if (isDuplicate(coppied))
//            throw new EmfException("A control Program named '" + coppied.getName() + "' already exists.");
//
//        coppied.setCreator(session.user());
//        coppied.setLastModifiedDate(new Date());
//        service().addControlProgram(coppied);
//    }
    
    public void doSaveCopiedPrograms(int id, User creator) throws EmfException {
        service().copyControlProgram(id, session.user());
    }
    
//    private boolean isDuplicate(ControlProgram newProgram) throws EmfException {
//        return (service().isDuplicateName(newProgram.getName()) != 0);
////        ControlProgram[] strategies = service().getControlStrategies();
////        for (int i = 0; i < strategies.length; i++) {
////            if (strategies[i].getName().equals(newProgram.getName()))
////                return true;
////        }
////
////        return false;
//    }

    public LightControlMeasure[] getControlMeasures() {
        return controlMeasures;
    }

    public void loadControlMeasures() throws EmfException  {
        controlMeasures = session.controlMeasureService().getLightControlMeasures();
    }
}
