package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;

public class ViewControlMeasureWindow extends EditControlMeasureWindow {

    public ViewControlMeasureWindow(EmfConsole parent, EmfSession session, DesktopManager desktopManager, CostYearTable costYearTable) {
        super(parent, session, desktopManager, costYearTable);
    }

    public void display(ControlMeasure measure) {
//        super.display(measure);

        setWindowTitle(measure);
        buildDisplay(measure);
        viewOnly();
        super.display();
        super.resetChanges();
    }

    private void setWindowTitle(ControlMeasure measure) {
        super.setTitle("View Control Measure: " + measure.getName());
        super.setName("viewControlMeasure" + measure.getId());
    }

    private void viewOnly() {
        // NOTE Auto-generated method stub
        saveButton.setVisible(false);
        controlMeasureSccTabView.viewOnly();
        controlMeasureEfficiencyTabView.viewOnly();
        editableCMSummaryTabView.viewOnly();
        controlMeasureEquationTabView.viewOnly();
        
    }
}
