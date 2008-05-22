package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlprogram.ControlProgramManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;

public class EditControlProgramMeasuresTabPresenter  implements EditControlProgramTabPresenter {
    private EditControlProgramMeasuresTab view;
    
    private EmfSession session;
    
    private ControlProgram controlProgram;

    private ControlProgramManagerPresenter controlProgramManagerPresenter;

    public EditControlProgramMeasuresTabPresenter(EditControlProgramMeasuresTab view, 
            ControlProgram controlProgram, EmfSession session, 
            ControlProgramManagerPresenter controlProgramManagerPresenter) {
        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
        this.controlProgramManagerPresenter = controlProgramManagerPresenter;
    }
    
    public void doDisplay() throws EmfException  {
        view.observe(this);
        view.display(this.controlProgram);
    }

    public ControlMeasureClass[] getAllClasses() throws EmfException {
        return session.controlMeasureService().getMeasureClasses();
    }

    public ControlMeasureClass[] getControlMeasureClasses() {
        return new ControlMeasureClass[] {};//strategy.getControlMeasureClasses();
    }

    public ControlStrategyMeasure[] getControlMeasures() {
        return new ControlStrategyMeasure[] {};//strategy.getControlMeasures();
    }

    public LightControlMeasure[] getAllControlMeasures() {
        return controlProgramManagerPresenter.getControlMeasures();
    }

    public void doChangeControlProgramType(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub
    }

    public void doRefresh(ControlProgram controlProgram) {
        // NOTE Auto-generated method stub
    }

    public void doSave(ControlProgram controlProgram) {
        // NOTE Auto-generated method stub
    }
}
