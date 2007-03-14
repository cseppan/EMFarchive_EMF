package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public interface ControlMeasurePresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void doModify();

    void doRefresh(ControlMeasure controlMeasure);

    void set(ControlMeasureTabView effTabView);

    void set(ControlMeasureSccTabView effTabView);

    void set(ControlMeasureEfficiencyTabView effTabView);
}