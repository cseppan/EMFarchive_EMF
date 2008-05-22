package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlProgram;

public interface EditControlProgramView extends ManagedView {

    void observe(EditControlProgramPresenter presenter);

    void display(ControlProgram controlProgram);
    
    void refresh(ControlProgram controlProgram);
    
    void notifyLockFailure(ControlProgram controlProgram);

    public void startControlMeasuresRefresh();

    public void endControlMeasuresRefresh();
    
    void signalChanges();    
}
