package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public interface ControlMeasuresEditorView extends ManagedView {

    void observe(ControlMeasuresEditorPresenter presenter);

    void display(ControlMeasure measure, String newOrEdit);

    void showError(String message);

    void notifyLockFailure(ControlMeasure measure);

}
