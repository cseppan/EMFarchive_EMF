package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public interface ControlMeasurePresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void doModify();

    void doRefresh(ControlMeasure controlMeasure);

    void set(ControlMeasureSummaryTab sumaryTabView);

    void set(ControlMeasureSccTabView effTabView);

    void set(ControlMeasureEfficiencyTabView effTabView);
    
    void set(ControlMeasureEquationTab equationTabView);

    void set(ControlMeasurePropertyTab propertyTabView);

    void fireTracking();

    Pollutant[] getPollutants() throws EmfException;
}