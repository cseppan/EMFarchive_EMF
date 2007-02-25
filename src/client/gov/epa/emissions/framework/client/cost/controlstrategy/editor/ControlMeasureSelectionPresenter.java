package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;

public class ControlMeasureSelectionPresenter {

    private ControlMeasureTableData tableData;
    private EditControlStrategyMeasuresTab parentView;
    private LightControlMeasure[] controlMeasures;

    public ControlMeasureSelectionPresenter(EditControlStrategyMeasuresTab parentView, ControlMeasureSelectionView view, 
            EmfSession session, LightControlMeasure[] controlMeasures) {
        this.parentView = parentView;
        this.controlMeasures = controlMeasures;
    }

    public void display(ControlMeasureSelectionView view) throws Exception {
        view.observe(this);
        this.tableData = new ControlMeasureTableData(controlMeasures);
        view.display(tableData);

    }

    public void doAdd(LightControlMeasure[] cms) {
        parentView.add(cms);
    }

}
