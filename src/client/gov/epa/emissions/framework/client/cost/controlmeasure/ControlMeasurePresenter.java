package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;

public interface ControlMeasurePresenter {

    void doDisplay(String newOrEdit) throws EmfException;
    
    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void set(EditableCMSummaryTabView summary);

}