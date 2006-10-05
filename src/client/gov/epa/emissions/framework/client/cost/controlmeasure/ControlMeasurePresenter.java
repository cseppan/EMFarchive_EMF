package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;

public interface ControlMeasurePresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void set(ControlMeasureTabView effTabView);

    void set(ControlMeasureSccTabView effTabView);

    void set(ControlMeasureEfficiencyTabView effTabView);
}