package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;

public interface ControlMeasuresEditorPresenter {

    void doDisplay(String newOrEdit) throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void set(EditableCMSummaryTabView summary);

}